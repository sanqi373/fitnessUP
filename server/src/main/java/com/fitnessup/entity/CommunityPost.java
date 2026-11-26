package com.fitnessup.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "community_post")
public class CommunityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String images; // JSON格式

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer likes;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer comments;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Transient
    private java.util.Map<String, String> user;

    @Transient
    private String time;

    @Transient
    private Boolean liked;

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
    }

    public CommunityPost() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }
    public Integer getComments() { return comments; }
    public void setComments(Integer comments) { this.comments = comments; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public java.util.Map<String, String> getUser() {
        return user == null ? java.util.Map.of("name", "健身达人", "avatar", "") : user;
    }
    public void setUser(java.util.Map<String, String> user) { this.user = user; }
    public String getTime() { return time != null ? time : (createTime == null ? "" : createTime.toString()); }
    public void setTime(String time) { this.time = time; }
    public Boolean getLiked() { return liked == null ? Boolean.FALSE : liked; }
    public void setLiked(Boolean liked) { this.liked = liked; }
}
