package com.fitnessup.config;

import com.fitnessup.entity.CommunityPost;
import com.fitnessup.entity.Course;
import com.fitnessup.entity.DietRecommendation;
import com.fitnessup.entity.TrainingPlan;
import com.fitnessup.entity.UserAchievement;
import com.fitnessup.entity.User;
import com.fitnessup.repository.CommunityPostRepository;
import com.fitnessup.repository.CourseRepository;
import com.fitnessup.repository.DietRecommendationRepository;
import com.fitnessup.repository.TrainingPlanRepository;
import com.fitnessup.repository.UserAchievementRepository;
import com.fitnessup.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashSet;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeDemoData(
            CourseRepository courseRepository,
            TrainingPlanRepository planRepository,
            DietRecommendationRepository dietRepository,
            CommunityPostRepository postRepository,
            UserAchievementRepository achievementRepository,
            UserRepository userRepository,
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper,
            TransactionTemplate transactions) {
        return args -> {
            migrateLegacyUsers(jdbcTemplate, userRepository, passwordEncoder);
            if (courseRepository.count() == 0) {
                courseRepository.saveAll(List.of(
                        course("30天马甲线挑战", "陈教练", "腹肌", "中级", "30天", 15, 12800, 4.8, 0D, "专为久坐一族设计，每天15分钟，30天练出马甲线"),
                        course("HIIT 高效燃脂", "李教练", "燃脂", "高级", "21天", 10, 9600, 4.7, 0D, "高强度间歇训练，每次20分钟，燃脂效果持续48小时"),
                        course("新手入门指南", "王教练", "综合", "初级", "14天", 7, 21500, 4.9, 0D, "从零开始，系统讲解健身基础知识与动作要领"),
                        course("瑜伽身心平衡", "张教练", "瑜伽", "初级", "28天", 12, 7800, 4.6, 29.9D, "缓解压力、改善体态的系统瑜伽课程"),
                        course("增肌塑形计划", "刘教练", "增肌", "高级", "60天", 24, 5400, 4.8, 49.9D, "科学分化训练，胸背腿肩臂每周循环"),
                        course("办公室拉伸放松", "赵教练", "拉伸", "初级", "7天", 5, 18600, 4.5, 0D, "每天5分钟，告别肩颈酸痛")
                ));
            }
            initializeTrainingPlans(planRepository, objectMapper, jdbcTemplate, transactions);
            initializeDietCatalog(dietRepository, jdbcTemplate, transactions);
            initializeCommunityAndGrowth(postRepository, jdbcTemplate, transactions);
            transactions.executeWithoutResult(status -> {
                if (jdbcTemplate.update("INSERT IGNORE INTO app_data_migration (name) VALUES ('workout-daily-energy')") == 0) return;
                // Older sessions contain only accumulated elapsed time, not individual running intervals.
                jdbcTemplate.update("""
                        INSERT INTO workout_session_day (user_id, session_id, workout_date, elapsed_ms, estimated_kcal)
                        SELECT w.user_id, w.session_id, COALESCE(DATE(w.finished_at), ?), w.elapsed_ms,
                               CASE WHEN p.duration > 0 AND p.calories > 0
                               THEN w.elapsed_ms * CAST(p.calories AS DECIMAL(24,9)) / (p.duration * 60000.0) ELSE 0 END
                        FROM workout_session w LEFT JOIN training_plan p ON p.id = w.plan_id
                        WHERE w.elapsed_ms > 0
                        """, LocalDate.now(java.time.ZoneId.of("Asia/Shanghai")));
            });
            if (achievementRepository.count() == 0) {
                achievementRepository.saveAll(List.of(
                        achievement("初出茅庐", "完成首次训练", "medal-o", 100, true),
                        achievement("坚持一周", "连续训练7天", "fire-o", 100, true),
                        achievement("百炼成钢", "累计训练100天", "shield-o", 0, false),
                        achievement("卡路里杀手", "累计消耗50000大卡", "flame-o", 0, false),
                        achievement("全能战士", "完成所有训练类别", "star-o", 0, false),
                        achievement("社交达人", "发帖获100赞", "good-job-o", 0, false)
                ));
            }
        };
    }

    private Course course(String title, String instructor, String category, String difficulty, String durationLabel,
                          int lessons, int enrolled, double rating, double price, String description) {
        Course value = new Course();
        value.setName(title); value.setInstructor(instructor); value.setCategory(category);
        value.setDifficulty(difficulty); value.setDuration(lessons); value.setMaxStudents(100000);
        value.setEnrolledCount(enrolled); value.setPrice(price); value.setStatus("online");
        value.setStartTime(LocalDateTime.now()); value.setEndTime(LocalDateTime.now().plusDays(60));
        value.setDescription(description); value.setTitle(title); value.setDurationLabel(durationLabel);
        value.setTotalLessons(lessons); value.setDesc(description);
        return value;
    }

    private void initializeTrainingPlans(TrainingPlanRepository repository, ObjectMapper mapper,
                                         JdbcTemplate jdbc, TransactionTemplate transactions) throws IOException {
        List<TrainingPlan> catalog;
        try (var stream = new ClassPathResource("training-plans.json").getInputStream()) {
            catalog = mapper.readValue(stream, new TypeReference<>() {});
        }
        // A committed version also preserves subsequent edits or deletions through the plans API.
        jdbc.execute("CREATE TABLE IF NOT EXISTS training_catalog_version (version INT PRIMARY KEY) ENGINE=InnoDB");
        transactions.executeWithoutResult(status -> {
            if (jdbc.update("INSERT IGNORE INTO training_catalog_version (version) VALUES (1)") == 0) return;
            var names = new HashSet<String>();
            for (TrainingPlan existing : repository.findByUserId(1L)) {
                names.add(existing.getName());
                if (existing.getDescription() == null || existing.getDescription().isBlank()) {
                    catalog.stream().filter(plan -> plan.getName().equals(existing.getName())).findFirst()
                            .ifPresent(plan -> existing.setDescription(plan.getDescription()));
                }
            }
            for (TrainingPlan plan : catalog) {
                if (names.add(plan.getName())) {
                    plan.setUserId(1L);
                    plan.setCatalogItem(true);
                    plan.setStatus("active");
                    repository.save(plan);
                }
            }
        });
        transactions.executeWithoutResult(status -> {
            if (jdbc.update("INSERT IGNORE INTO training_catalog_version (version) VALUES (2)") == 0) return;
            for (TrainingPlan existing : repository.findByUserId(1L)) {
                catalog.stream().filter(seed -> seed.getName().equals(existing.getName())).findFirst().ifPresent(seed -> {
                    existing.setCatalogItem(true);
                    if (existing.getCover() == null || existing.getCover().isBlank()) existing.setCover(seed.getCover());
                });
            }
        });
    }

    private void initializeDietCatalog(DietRecommendationRepository repository, JdbcTemplate jdbc,
                                       TransactionTemplate transactions) {
        jdbc.execute("CREATE TABLE IF NOT EXISTS app_data_migration (name VARCHAR(80) PRIMARY KEY) ENGINE=InnoDB");
        transactions.executeWithoutResult(status -> {
            if (jdbc.update("INSERT IGNORE INTO app_data_migration (name) VALUES (?)", "shared-diet-catalog") == 0) return;
            var catalog = List.of(
                    diet("鸡胸肉沙拉", "lunch", 320, 35D, 8D, 15D, "/recipes/chicken-salad.webp"),
                    diet("全麦三明治", "breakfast", 280, 20D, 6D, 35D, "/recipes/wholewheat-sandwich.webp"),
                    diet("三文鱼糙米饭", "lunch", 450, 38D, 12D, 40D, "/recipes/salmon-rice.webp"),
                    diet("蛋白奶昔", "snack", 180, 30D, 3D, 10D, "/recipes/protein-shake.webp"),
                    diet("蔬菜牛肉汤", "dinner", 350, 28D, 10D, 25D, "/recipes/vegetable-beef-soup.webp"),
                    diet("水果酸奶碗", "breakfast", 200, 12D, 4D, 30D, "/recipes/fruit-yogurt.webp")
            );
            var legacy = repository.findByUserId(1L);
            for (DietRecommendation seed : catalog) {
                var item = legacy.stream().filter(row -> row.getName().equals(seed.getName())
                                && row.getMealType().equals(seed.getMealType())).findFirst().orElse(seed);
                item.setCatalogItem(true);
                if (item.getImage() == null || item.getImage().isBlank()) item.setImage(seed.getImage());
                repository.save(item);
            }
        });
    }

    private DietRecommendation diet(String name, String mealType, int calories, double protein, double fat, double carbs, String cover) {
        DietRecommendation value = new DietRecommendation();
        value.setUserId(1L); value.setName(name); value.setMealType(mealType); value.setCalories(calories);
        value.setProtein(protein); value.setFat(fat); value.setCarbs(carbs); value.setRecommendedAt(LocalDate.now());
        value.setImage(cover);
        return value;
    }

    private CommunityPost post(String content, int likes, int comments) {
        CommunityPost value = new CommunityPost();
        value.setUserId(1L); value.setContent(content); value.setImages("[]");
        value.setLikes(likes); value.setComments(comments);
        return value;
    }

    private void initializeCommunityAndGrowth(CommunityPostRepository posts, JdbcTemplate jdbc,
                                              TransactionTemplate transactions) {
        jdbc.execute("CREATE TABLE IF NOT EXISTS app_data_migration (name VARCHAR(80) PRIMARY KEY) ENGINE=InnoDB");
        transactions.executeWithoutResult(status -> {
            if (jdbc.update("INSERT IGNORE INTO app_data_migration (name) VALUES (?)", "account-growth-baseline") == 1) {
                jdbc.update("UPDATE `user` SET level = 1, exp = 0 WHERE level <> 1 OR exp <> 0 OR level IS NULL OR exp IS NULL");
            }
            if (jdbc.update("INSERT IGNORE INTO app_data_migration (name) VALUES (?)", "community-feed-20") == 0) return;
            long remaining = 20 - posts.count();
            if (remaining <= 0) return;
            var existing = new HashSet<String>();
            posts.findAll().forEach(post -> existing.add(post.getContent()));
            var samples = List.of(
                    post("坚持晨跑第 30 天！配速终于进 5 分钟了，这种感觉太爽了 💪", 238, 42),
                    post("分享一套睡前拉伸序列，5个动作缓解一天的疲劳~", 156, 28),
                    post("新 PR！深蹲 120kg 5x5 完成 ✅ 三个月前还只能蹲 80，进步看得见", 412, 67),
                    post("本周六晚上 8 点，陈教练将带来核心力量训练直播课，免费参加", 89, 15),
                    post("午休绕园区快走一圈，正好听完一期播客。下次想试试把最后五分钟留给慢走。", 26, 3),
                    post("第一次用弹力带做划船。把回程放慢以后，终于能分清背部发力和耸肩的区别了。", 37, 6),
                    post("给跳绳换了合适的长度，今天第一次连续跳过一分钟。大家会用什么歌找节奏？", 45, 8),
                    post("周末沿河跑了五公里。去程逆风有点吃力，返程索性收起手表，慢慢看风景。", 58, 4),
                    post("今天的训练搭子是家里的椅子。扶着椅背做分腿蹲，左右两边的稳定性差别很明显。", 31, 5),
                    post("上完单车课，记下一个小习惯：先调好座高，再花几分钟轻松踩踏。", 24, 2),
                    post("第一次练树式，靠墙站了三轮。最后一轮手离开墙面，稳稳站了十秒！", 63, 7),
                    post("整理训练笔记时发现，同一副哑铃从每组八次练到了十二次。今天给自己记一笔。", 74, 9),
                    post("下雨没能出门跑步，换成了无跳跃间歇。收好瑜伽垫的时候，楼下邻居应该还在睡。", 51, 3),
                    post("晨练后试了燕麦、酸奶和蓝莓的组合。前一晚备好，第二天出门前能多坐五分钟。", 42, 6),
                    post("泡沫轴滚完小腿，今天就到这里。休息日能把动作做慢，也算按计划打卡。", 29, 2),
                    post("在公园发现一段有树荫的平路，刚好适合走跑交替。已经把起点记在地图上了。", 35, 4),
                    post("把俯卧撑拍下来才发现最后两次动作有点散。下次少做两次，先把节奏练稳。", 66, 8),
                    post("久坐一上午，跟着桌边瑜伽做了十分钟。最喜欢坐姿侧弯，左右各做一遍很舒服。", 48, 5),
                    post("今天和朋友轮流计时做拳击步伐。第一次合作，光是记住先动哪只脚就笑了好几次。", 53, 6),
                    post("这周第一次完成三次训练：一次力量、一次快走、一次瑜伽。每次不长，但都在日历上留下了记录。", 81, 10)
            );
            posts.saveAll(samples.stream().filter(post -> existing.add(post.getContent())).limit(remaining).toList());
        });
    }

    private UserAchievement achievement(String name, String description, String icon, int progress, boolean achieved) {
        UserAchievement value = new UserAchievement();
        value.setUserId(1L); value.setName(name); value.setDescription(description); value.setIcon(icon);
        value.setProgress(progress); value.setAchieved(achieved);
        return value;
    }

    private void migrateLegacyUsers(JdbcTemplate jdbcTemplate, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        try {
            jdbcTemplate.query("SELECT phone, password, nickname FROM users", resultSet -> {
                String phone = resultSet.getString("phone");
                if (!userRepository.existsByPhone(phone)) {
                    User user = new User();
                    user.setPhone(phone);
                    user.setPassword(passwordEncoder.encode(resultSet.getString("password")));
                    user.setNickname(resultSet.getString("nickname"));
                    user.setGender(0); user.setLevel(1); user.setExp(0);
                    userRepository.save(user);
                }
            });
        } catch (org.springframework.dao.DataAccessException ignored) {
            // The legacy Node.js table does not exist on a fresh database.
        }
    }
}
