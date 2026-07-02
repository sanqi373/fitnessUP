package com.fitnessup.controller;

import com.fitnessup.common.ApiResult;
import com.fitnessup.entity.User;
import com.fitnessup.security.JwtUtil;
import com.fitnessup.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResult<?>> register(@RequestBody User user) {
        if (user.getPhone() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest().body(ApiResult.error("手机号和密码不能为空"));
        }
        if (userService.existsByPhone(user.getPhone())) {
            return ResponseEntity.badRequest().body(ApiResult.error("该手机号已注册"));
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getNickname() == null) {
            user.setNickname("健身达人");
        }
        if (user.getGender() == null) {
            user.setGender(0);
        }
        if (user.getLevel() == null) {
            user.setLevel(1);
        }
        if (user.getExp() == null) {
            user.setExp(0);
        }
        User saved = userService.createUser(user);
        String token = jwtUtil.generateToken(saved.getId(), saved.getPhone());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", saved);
        return ResponseEntity.ok(ApiResult.success(result));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResult<?>> login(@RequestBody Map<String, String> loginData) {
        String phone = loginData.get("phone");
        String password = loginData.get("password");
        if (phone == null || password == null) {
            return ResponseEntity.badRequest().body(ApiResult.error("手机号和密码不能为空"));
        }
        Optional<User> userOpt = userService.findByPhone(phone);
        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            return ResponseEntity.status(401).body(ApiResult.error(401, "手机号或密码错误"));
        }
        User user = userOpt.get();
        String token = jwtUtil.generateToken(user.getId(), user.getPhone());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return ResponseEntity.ok(ApiResult.success(result));
    }
}
