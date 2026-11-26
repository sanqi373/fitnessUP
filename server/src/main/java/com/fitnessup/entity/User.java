package com.fitnessup.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@DynamicUpdate
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(nullable = false, length = 100)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String avatar;

    @Column(nullable = false)
    private Integer gender; // 0-未知 1-男 2-女

    private LocalDate birthday;

    @Column(columnDefinition = "DOUBLE(10,2)")
    private Double height;

    @Column(columnDefinition = "DOUBLE(10,2)")
    private Double weight;

    @Column(nullable = false, columnDefinition = "int default 1")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer level = 1;

    @Column(nullable = false, columnDefinition = "int default 0")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer exp = 0;

    @Column(name = "calorie_goal")
    private Long calorieGoal;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getCalorieGoal() { return calorieGoal; }

    @Transient
    private java.util.Map<String, Object> stats;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public User() {}

    // Getters and Setters
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }
    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }
    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public Integer getExp() { return exp; }
    public void setExp(Integer exp) { this.exp = exp; }
    public java.util.Map<String, Object> getStats() { return stats; }
    public void setStats(java.util.Map<String, Object> stats) { this.stats = stats; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
