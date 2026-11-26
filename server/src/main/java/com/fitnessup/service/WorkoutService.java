package com.fitnessup.service;

import com.fitnessup.repository.TrainingPlanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Instant;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.fitnessup.entity.TrainingPlan;

@Service
public class WorkoutService {
    private static final ZoneId CHECK_IN_ZONE = ZoneId.of("Asia/Shanghai");
    private final JdbcClient jdbc;
    private final TrainingPlanRepository plans;

    public WorkoutService(JdbcClient jdbc, TrainingPlanRepository plans) {
        this.jdbc = jdbc;
        this.plans = plans;
    }

    public enum Action { start, pause, resume, stop }
    public record Timer(String id, boolean running, long elapsedMs) {}
    public record Progress(long trainedSeconds, Timer timer) {}

    @Transactional(readOnly = true)
    public Progress progress(long userId, long planId) {
        long total = jdbc.sql("""
                SELECT COALESCE(SUM(elapsed_ms), 0) FROM workout_session
                WHERE user_id=? AND plan_id=? AND finished_at IS NOT NULL
                """).params(userId, planId).query(Long.class).single();
        Timer timer = jdbc.sql("""
                SELECT session_id, elapsed_ms, running_since_ms FROM workout_session
                WHERE user_id=? AND open_plan_id=?
                """).params(userId, planId).query((row, index) -> {
                    Long since = row.getObject("running_since_ms", Long.class);
                    long runningMs = since == null ? 0 : Math.max(0, System.currentTimeMillis() - since);
                    return new Timer(row.getString("session_id"), since != null, row.getLong("elapsed_ms") + runningMs);
                }).optional().orElse(null);
        return new Progress(total / 1000, timer);
    }

    @Transactional
    public Progress change(long userId, long planId, UUID sessionId, Action action) {
        // Serialize changes from multiple tabs before checking plan visibility and session state.
        jdbc.sql("SELECT id FROM `user` WHERE id=? FOR UPDATE").param(userId).query(Long.class).single();
        TrainingPlan plan = plans.findVisibleById(planId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "训练计划不存在"));
        long now = System.currentTimeMillis();
        String id = sessionId.toString();
        if (action == Action.start) {
            jdbc.sql("""
                    INSERT INTO workout_session (user_id, session_id, plan_id, running_since_ms)
                    VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE elapsed_ms=elapsed_ms
                    """).params(userId, id, planId, now).update();
        } else if (action == Action.resume) {
            jdbc.sql("""
                    UPDATE workout_session SET running_since_ms=?
                    WHERE user_id=? AND session_id=? AND open_plan_id=? AND running_since_ms IS NULL
                    """).params(now, userId, id, planId).update();
        } else {
            jdbc.sql("""
                    SELECT running_since_ms FROM workout_session
                    WHERE user_id=? AND session_id=? AND open_plan_id=? AND running_since_ms IS NOT NULL
                    """).params(userId, id, planId).query(Long.class).optional()
                    .ifPresent(start -> recordInterval(userId, id, plan, start, now));
            String finished = action == Action.stop ? ", finished_at=CURRENT_TIMESTAMP(3)" : "";
            jdbc.sql("""
                    UPDATE workout_session SET elapsed_ms=elapsed_ms+GREATEST(0, ?-COALESCE(running_since_ms, ?)),
                        running_since_ms=NULL
                    """ + finished + " WHERE user_id=? AND session_id=? AND open_plan_id=?")
                    .params(now, now, userId, id, planId).update();
        }
        return progress(userId, planId);
    }

    private void recordInterval(long userId, String id, TrainingPlan plan, long start, long end) {
        while (start < end) {
            LocalDate date = Instant.ofEpochMilli(start).atZone(CHECK_IN_ZONE).toLocalDate();
            long midnight = date.plusDays(1).atStartOfDay(CHECK_IN_ZONE).toInstant().toEpochMilli();
            long until = Math.min(end, midnight);
            long millis = until - start;
            BigDecimal kcal = BigDecimal.ZERO;
            if (plan.getCalories() != null && plan.getCalories() > 0 && plan.getDuration() > 0) {
                kcal = BigDecimal.valueOf(millis).multiply(BigDecimal.valueOf(plan.getCalories()))
                        .divide(BigDecimal.valueOf(plan.getDuration() * 60000L), 9, RoundingMode.HALF_UP);
            }
            jdbc.sql("""
                    INSERT INTO workout_session_day (user_id, session_id, workout_date, elapsed_ms, estimated_kcal)
                    VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE elapsed_ms=elapsed_ms+?, estimated_kcal=estimated_kcal+?
                    """).params(userId, id, date, millis, kcal, millis, kcal).update();
            start = until;
        }
    }

    @Transactional
    public Map<String, Object> checkIn(long userId) {
        LocalDate today = LocalDate.now(CHECK_IN_ZONE);
        jdbc.sql("""
                INSERT INTO user_check_in (user_id, check_in_date) VALUES (?, ?)
                ON DUPLICATE KEY UPDATE check_in_date=user_check_in.check_in_date
                """).params(userId, today).update();
        return stats(userId, today);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> stats(long userId) {
        return stats(userId, LocalDate.now(CHECK_IN_ZONE));
    }

    private Map<String, Object> stats(long userId, LocalDate today) {
        record Energy(long todayMs, BigDecimal todayCalories, BigDecimal totalCalories) {}
        var energy = jdbc.sql("""
                SELECT COALESCE(SUM(CASE WHEN d.workout_date=? THEN d.elapsed_ms END), 0) AS today_ms,
                       ROUND(COALESCE(SUM(CASE WHEN d.workout_date=? THEN d.estimated_kcal END), 0), 2) AS today_calories,
                       ROUND(COALESCE(SUM(d.estimated_kcal), 0), 2) AS total_calories
                FROM workout_session_day d JOIN workout_session w USING (user_id, session_id)
                WHERE d.user_id=? AND w.finished_at IS NOT NULL
                """).params(today, today, userId).query(Energy.class).single();
        long totalSeconds = jdbc.sql("""
                SELECT COALESCE(SUM(course_seconds), 0) FROM (
                    SELECT FLOOR(SUM(elapsed_ms) / 1000) AS course_seconds FROM workout_session
                    WHERE user_id=? AND finished_at IS NOT NULL GROUP BY plan_id
                ) AS completed_courses
                """).param(userId).query(Long.class).single();
        var dates = jdbc.sql("""
                SELECT check_in_date FROM user_check_in
                WHERE user_id=? AND check_in_date<=? ORDER BY check_in_date DESC
                """).params(userId, today).query(LocalDate.class).list();
        boolean checkedIn = !dates.isEmpty() && today.equals(dates.getFirst());
        LocalDate expected = checkedIn ? today : today.minusDays(1);
        int streak = 0;
        for (LocalDate date : dates) {
            if (!date.equals(expected)) break;
            streak++;
            expected = expected.minusDays(1);
        }
        return Map.of("totalTrainSeconds", totalSeconds, "totalTrainMinutes", totalSeconds / 60,
                "todayTrainSeconds", energy.todayMs() / 1000, "todayCalories", energy.todayCalories(),
                "totalTrainDays", dates.size(), "streakDays", streak, "totalCalories", energy.totalCalories(),
                "checkedInToday", checkedIn, "checkInDate", today,
                "nextCheckInAt", today.plusDays(1).atStartOfDay(CHECK_IN_ZONE).toInstant());
    }
}
