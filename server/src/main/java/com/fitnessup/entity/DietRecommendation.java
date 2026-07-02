package com.fitnessup.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "diet_recommendation")
public class DietRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "meal_type", nullable = false, length = 20)
    private String mealType; // breakfast/lunch/dinner/snack

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer calories;

    @Column(columnDefinition = "DOUBLE(10,2)")
    private Double protein;

    @Column(columnDefinition = "DOUBLE(10,2)")
    private Double fat;

    @Column(columnDefinition = "DOUBLE(10,2)")
    private Double carbs;

    @Column(length = 255)
    private String image;

    @Column(name = "recommended_at", nullable = false)
    private LocalDate recommendedAt;

    public DietRecommendation() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getCalories() { return calories; }
    public void setCalories(Integer calories) { this.calories = calories; }
    public Double getProtein() { return protein; }
    public void setProtein(Double protein) { this.protein = protein; }
    public Double getFat() { return fat; }
    public void setFat(Double fat) { this.fat = fat; }
    public Double getCarbs() { return carbs; }
    public void setCarbs(Double carbs) { this.carbs = carbs; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public LocalDate getRecommendedAt() { return recommendedAt; }
    public void setRecommendedAt(LocalDate recommendedAt) { this.recommendedAt = recommendedAt; }
}
