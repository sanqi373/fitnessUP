package com.fitnessup.repository;

import com.fitnessup.entity.DietRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DietRecommendationRepository extends JpaRepository<DietRecommendation, Long> {
    List<DietRecommendation> findByUserId(Long userId);

    @Query("select item from DietRecommendation item where item.catalogItem = true or item.userId = :userId order by item.id")
    List<DietRecommendation> findVisible(Long userId);

    @Query("select item from DietRecommendation item where item.id = :id and (item.catalogItem = true or item.userId = :userId)")
    Optional<DietRecommendation> findVisibleById(Long id, Long userId);

    @Query(value = """
            SELECT item.* FROM diet_recommendation item
            JOIN saved_diet_plan saved ON saved.recipe_id = item.id
            WHERE saved.user_id = :userId AND (item.catalog_item OR item.user_id = :userId)
            ORDER BY saved.saved_at DESC, item.id DESC
            """, nativeQuery = true)
    List<DietRecommendation> findSaved(Long userId);

    @Query(value = "SELECT COUNT(*) FROM saved_diet_plan WHERE user_id = :userId AND recipe_id = :id", nativeQuery = true)
    int countSaved(Long userId, Long id);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO saved_diet_plan (user_id, recipe_id)
            SELECT :userId, item.id FROM diet_recommendation item
            WHERE item.id = :id AND (item.catalog_item OR item.user_id = :userId)
            ON DUPLICATE KEY UPDATE recipe_id = saved_diet_plan.recipe_id
            """, nativeQuery = true)
    void addSaved(Long userId, Long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM saved_diet_plan WHERE user_id = :userId AND recipe_id = :id", nativeQuery = true)
    void removeSaved(Long userId, Long id);
}
