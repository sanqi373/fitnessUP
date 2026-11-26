package com.fitnessup.controller;

import com.fitnessup.common.ApiResult;
import com.fitnessup.entity.User;
import com.fitnessup.service.UserService;
import com.fitnessup.service.WorkoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final WorkoutService workouts;

    public UserController(UserService userService, WorkoutService workouts) {
        this.userService = userService;
        this.workouts = workouts;
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResult<?>> getUserInfo() {
        User user = currentUser();
        if (user == null) return ResponseEntity.status(404).body(ApiResult.error(404, "用户不存在"));
        return ResponseEntity.ok(ApiResult.success(toUserInfo(user)));
    }

    @PutMapping("/info")
    @Transactional
    public ResponseEntity<ApiResult<?>> updateUserInfo(@RequestBody User input) {
        User current = currentUser();
        if (current == null) return ResponseEntity.status(404).body(ApiResult.error(404, "用户不存在"));
        if (input.getGender() != null && (input.getGender() < 0 || input.getGender() > 2))
            return ResponseEntity.badRequest().body(ApiResult.error(400, "请选择有效的性别"));
        if (input.getBirthday() != null && input.getBirthday().isAfter(LocalDate.now()))
            return ResponseEntity.badRequest().body(ApiResult.error(400, "生日不能晚于今天"));
        if (input.getPhone() != null && !input.getPhone().equals(current.getPhone())) {
            if (!input.getPhone().matches("^1\\d{10}$"))
                return ResponseEntity.badRequest().body(ApiResult.error(400, "请输入正确的11位手机号"));
            if (userService.existsByPhone(input.getPhone()))
                return ResponseEntity.status(409).body(ApiResult.error(409, "该手机号已被使用"));
            current.setPhone(input.getPhone());
        }
        if (input.getBio() != null) current.setBio(input.getBio());
        if (input.getNickname() != null) current.setNickname(input.getNickname());
        if (input.getAvatar() != null) current.setAvatar(input.getAvatar());
        if (input.getGender() != null) current.setGender(input.getGender());
        if (input.getBirthday() != null) current.setBirthday(input.getBirthday());
        if (input.getHeight() != null) current.setHeight(input.getHeight());
        if (input.getWeight() != null) current.setWeight(input.getWeight());
        return ResponseEntity.ok(ApiResult.success(toUserInfo(userService.updateUser(current))));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResult<?>> profileConflict(DataIntegrityViolationException error) {
        return ResponseEntity.status(409).body(ApiResult.error(409, "资料保存冲突，请检查手机号和输入内容后重试"));
    }

    public record CalorieGoal(Long calories) {}

    @PutMapping("/calorie-goal")
    public ResponseEntity<ApiResult<?>> setCalorieGoal(@AuthenticationPrincipal Long userId,
            @RequestBody CalorieGoal input, @RequestHeader(value = "If-None-Match", required = false) String precondition) {
        if (input.calories() == null || input.calories() < 0 || input.calories() > 9007199254740991L) {
            return ResponseEntity.badRequest().body(ApiResult.error(400, "请输入有效的非负整数"));
        }
        userService.setCalorieGoal(userId, input.calories(), "*".equals(precondition));
        return getUserInfo();
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResult<?>> getStats(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResult.success(workouts.stats(userId)));
    }

    @PutMapping("/check-in")
    public ApiResult<?> checkIn(@AuthenticationPrincipal Long userId) {
        return ApiResult.success(workouts.checkIn(userId));
    }

    private User currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Long id)) return null;
        Optional<User> user = userService.findById(id);
        return user.orElse(null);
    }

    private User toUserInfo(User user) {
        return user;
    }
}
