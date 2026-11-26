package com.fitnessup.repository;

import com.fitnessup.entity.TrainingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, Long> {
    List<TrainingPlan> findByUserId(Long userId);

    @Query("select item from TrainingPlan item where item.catalogItem = true or item.userId = :userId order by item.id")
    List<TrainingPlan> findVisible(Long userId);

    @Query("select item from TrainingPlan item where item.id = :id and (item.catalogItem = true or item.userId = :userId)")
    Optional<TrainingPlan> findVisibleById(Long id, Long userId);

    @Query(value = """
            SELECT item.* FROM training_plan item
            JOIN saved_training_plan saved ON saved.plan_id = item.id
            WHERE saved.user_id = :userId AND (item.catalog_item OR item.user_id = :userId)
            ORDER BY saved.saved_at DESC, item.id DESC
            """, nativeQuery = true)
    List<TrainingPlan> findSaved(Long userId);

    @Query(value = "SELECT COUNT(*) FROM saved_training_plan WHERE user_id = :userId AND plan_id = :id", nativeQuery = true)
    int countSaved(Long userId, Long id);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO saved_training_plan (user_id, plan_id)
            SELECT :userId, item.id FROM training_plan item
            WHERE item.id = :id AND (item.catalog_item OR item.user_id = :userId)
            ON DUPLICATE KEY UPDATE plan_id = saved_training_plan.plan_id
            """, nativeQuery = true)
    void addSaved(Long userId, Long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM saved_training_plan WHERE user_id = :userId AND plan_id = :id", nativeQuery = true)
    void removeSaved(Long userId, Long id);
}
