package com.fitnessup.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_plan")
public class TrainingPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String type; // strength/cardio/flexibility/recovery

    @Column(nullable = false)
    private Integer duration; // 分钟

    @Column
    private Integer calories; // 千卡

    @Column(columnDefinition = "TEXT")
    private String exercises; // JSON格式

    @Column(length = 20)
    private String difficulty; // easy/medium/hard

    @Column(nullable = false, length = 20)
    private String status; // draft/active/completed

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Transient
    private String desc;

    @Transient
    private String level;

    @Column(length = 255)
    private String cover;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean catalogItem;

    @JsonIgnore
    public boolean isCatalogItem() { return catalogItem; }
    public void setCatalogItem(boolean catalogItem) { this.catalogItem = catalogItem; }

    public TrainingPlan() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Integer getCalories() { return calories; }
    public void setCalories(Integer calories) { this.calories = calories; }
    @JsonProperty("exercises")
    public Object getExercises() {
        if (exercises == null || exercises.isBlank()) return java.util.List.of();
        try {
            return new ObjectMapper().readTree(exercises);
        } catch (Exception ignored) {
            return exercises;
        }
    }
    public void setExercises(Object exercises) {
        if (exercises == null) {
            this.exercises = null;
            return;
        }
        if (exercises instanceof String value) {
            this.exercises = value;
            return;
        }
        try {
            this.exercises = new ObjectMapper().writeValueAsString(exercises);
        } catch (Exception ignored) {
            this.exercises = exercises.toString();
        }
    }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDesc() { return desc != null ? desc : (description == null ? "" : description); }
    public void setDesc(String desc) { this.desc = desc; }
    public String getLevel() { return level != null ? level : difficulty; }
    public void setLevel(String level) { this.level = level; }
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
}
