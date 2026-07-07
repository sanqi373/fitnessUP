package com.fitnessup.service;

import com.fitnessup.entity.CommunityPost;
import com.fitnessup.repository.CommunityPostRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;

    public CommunityPostService(CommunityPostRepository communityPostRepository) {
        this.communityPostRepository = communityPostRepository;
    }

    public List<CommunityPost> getAllPosts() {
        return communityPostRepository.findAllByOrderByCreateTimeDesc();
    }

    public CommunityPost createPost(CommunityPost post) {
        return communityPostRepository.save(post);
    }
}
