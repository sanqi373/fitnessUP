package com.fitnessup.controller;

import com.fitnessup.common.ApiResult;
import com.fitnessup.entity.User;
import com.fitnessup.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResult<?>> getUserInfo() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResult.error(404, "用户不存在"));
        }
        return ResponseEntity.ok(ApiResult.success(userOpt.get()));
    }

    @PutMapping("/info")
    public ResponseEntity<ApiResult<?>> updateUserInfo(@RequestBody User user) {
        if (user.getId() == null) {
            return ResponseEntity.badRequest().body(ApiResult.error("用户ID不能为空"));
        }
        Optional<User> existingOpt = userService.findById(user.getId());
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResult.error(404, "用户不存在"));
        }
        User updated = userService.updateUser(user);
        return ResponseEntity.ok(ApiResult.success(updated));
    }
}
