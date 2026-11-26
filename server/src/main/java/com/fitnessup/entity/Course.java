package com.fitnessup.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "cover_image", length = 255)
    private String coverImage;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String instructor;

    @Column(nullable = false)
    private Integer duration;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 20)
    private String difficulty;

    @Column(name = "max_students", nullable = false)
    private Integer maxStudents;

    @Column(name = "enrolled_count", nullable = false, columnDefinition = "int default 0")
    private Integer enrolledCount;

    @Column(nullable = false, columnDefinition = "double default 0.0")
    private Double price;

    @Column(nullable = false, length = 20)
    private String status; // online/offline

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private String title;

    @Transient
    private java.util.Map<String, String> coach;

    @Transient
    private String durationLabel;

    @Transient
    private Integer totalLessons;

    @Transient
    private String desc;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Course() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public Integer getMaxStudents() { return maxStudents; }
    public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }
    public Integer getEnrolledCount() { return enrolledCount; }
    public void setEnrolledCount(Integer enrolledCount) { this.enrolledCount = enrolledCount; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getTitle() { return title != null ? title : name; }
    public void setTitle(String title) { this.title = title; }
    public java.util.Map<String, String> getCoach() {
        if (coach == null) return java.util.Map.of("name", instructor == null ? "" : instructor, "avatar", "");
        return coach;
    }
    public void setCoach(java.util.Map<String, String> coach) { this.coach = coach; }
    public String getDurationLabel() { return durationLabel != null ? durationLabel : String.valueOf(duration); }
    public void setDurationLabel(String durationLabel) { this.durationLabel = durationLabel; }
    public Integer getTotalLessons() { return totalLessons != null ? totalLessons : duration; }
    public void setTotalLessons(Integer totalLessons) { this.totalLessons = totalLessons; }
    public String getDesc() { return desc != null ? desc : description; }
    public void setDesc(String desc) { this.desc = desc; }
}
