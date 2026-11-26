package com.fitnessup.controller;

import com.fitnessup.common.ApiResult;
import com.fitnessup.entity.CommunityPost;
import com.fitnessup.service.CommunityPostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityPostService communityPostService;

    public CommunityController(CommunityPostService communityPostService) {
        this.communityPostService = communityPostService;
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResult<?>> getPosts() {
        List<CommunityPost> posts = communityPostService.getAllPosts();
        return ResponseEntity.ok(ApiResult.success(Map.of("list", posts, "total", posts.size())));
    }

    @PostMapping("/posts")
    public ResponseEntity<ApiResult<?>> createPost(@RequestBody CommunityPost post) {
        CommunityPost created = communityPostService.createPost(post);
        return ResponseEntity.ok(ApiResult.success(created));
    }
}
