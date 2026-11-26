package com.fitnessup.controller;

import com.fitnessup.common.ApiResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/body-records")
public class BodyRecordController {
    private final JdbcClient jdbc;

    public BodyRecordController(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public record Entry(
            @NotNull @PositiveOrZero Integer revision,
            @Positive @Max(300) @Digits(integer=3, fraction=1) BigDecimal height,
            @Positive @Max(1000) @Digits(integer=4, fraction=1) BigDecimal weight,
            @Positive @Max(100) @Digits(integer=3, fraction=1) BigDecimal bodyFat,
            @Positive @Max(500) @Digits(integer=3, fraction=1) BigDecimal waist,
            @Positive @Max(15000) @Digits(integer=5, fraction=1) BigDecimal basalMetabolism,
            @PositiveOrZero @Max(5000) @Digits(integer=4, fraction=1) BigDecimal carbs,
            @PositiveOrZero @Max(5000) @Digits(integer=4, fraction=1) BigDecimal protein,
            @PositiveOrZero @Max(5000) @Digits(integer=4, fraction=1) BigDecimal fat,
            @PositiveOrZero @Max(30000) @Digits(integer=5, fraction=1) BigDecimal water) {}

    @GetMapping
    public ApiResult<?> month(@AuthenticationPrincipal Long userId, @RequestParam YearMonth month) {
        return ApiResult.success(jdbc.sql("""
                SELECT record_date AS date, revision, height, weight, body_fat AS bodyFat, waist,
                       basal_metabolism AS basalMetabolism, carbs, protein, fat, water
                FROM body_record WHERE user_id = ? AND record_date BETWEEN ? AND ?
                ORDER BY record_date DESC
                """).params(userId, month.atDay(1), month.atEndOfMonth()).query().listOfRows());
    }

    @PutMapping("/{date}")
    public ResponseEntity<ApiResult<?>> save(@AuthenticationPrincipal Long userId,
            @PathVariable LocalDate date, @Valid @RequestBody Entry entry) {
        if (date.isAfter(LocalDate.now()) || date.getYear() < 1000)
            return ResponseEntity.badRequest().body(ApiResult.error(400, "请选择今天或之前的有效日期"));
        if (Stream.of(entry.height(), entry.weight(), entry.bodyFat(), entry.waist(), entry.basalMetabolism(),
                entry.carbs(), entry.protein(), entry.fat(), entry.water()).allMatch(Objects::isNull))
            return ResponseEntity.badRequest().body(ApiResult.error(400, "请至少填写一项数据"));
        // A new day is inserted once. Existing days use the version originally shown to the user.
        boolean creating = entry.revision() == 0;
        String statement = creating ? """
                INSERT INTO body_record (user_id, record_date, height, weight, body_fat, waist,
                    basal_metabolism, carbs, protein, fat, water)
                VALUES (:owner, :date, :height, :weight, :bodyFat, :waist, :basalMetabolism,
                    :carbs, :protein, :fat, :water)
                """ : """
                UPDATE body_record SET height=:height, weight=:weight, body_fat=:bodyFat, waist=:waist,
                    basal_metabolism=:basalMetabolism, carbs=:carbs, protein=:protein, fat=:fat,
                    water=:water, revision=revision+1
                WHERE user_id=:owner AND record_date=:date AND revision=:revision
                """;
        try {
            int rows = jdbc.sql(statement).param("owner", userId).param("date", date)
                    .param("revision", entry.revision()).param("height", entry.height())
                    .param("weight", entry.weight()).param("bodyFat", entry.bodyFat()).param("waist", entry.waist())
                    .param("basalMetabolism", entry.basalMetabolism()).param("carbs", entry.carbs())
                    .param("protein", entry.protein()).param("fat", entry.fat()).param("water", entry.water()).update();
            if (rows == 0) return conflict();
        } catch (DuplicateKeyException ex) {
            return conflict();
        }
        return ResponseEntity.ok(ApiResult.success(entry.revision() + 1));
    }

    private ResponseEntity<ApiResult<?>> conflict() {
        return ResponseEntity.status(409).body(ApiResult.error(409, "该日记录已更新，请重新加载后再编辑"));
    }
}
