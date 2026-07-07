package com.fitnessup.service;

import com.fitnessup.entity.UserAchievement;
import com.fitnessup.repository.UserAchievementRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAchievementService {

    private final UserAchievementRepository userAchievementRepository;

    public UserAchievementService(UserAchievementRepository userAchievementRepository) {
        this.userAchievementRepository = userAchievementRepository;
    }

    public List<UserAchievement> getAchievementsByUserId(Long userId) {
        return userAchievementRepository.findByUserId(userId);
    }
}
