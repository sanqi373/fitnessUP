package com.fitnessup.controller;

import com.fitnessup.common.ApiResult;
import com.fitnessup.entity.User;
import com.fitnessup.security.JwtUtil;
import com.fitnessup.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
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
        if (user.getPhone() == null || !user.getPhone().matches("^1\\d{10}$")) {
            return ResponseEntity.badRequest().body(ApiResult.error(400, "请输入正确的11位手机号"));
        }
        String password = user.getPassword();
        if (password == null || !password.matches("[A-Za-z0-9\\p{P}\\p{S}]{6,}")) {
            return ResponseEntity.badRequest().body(ApiResult.error(400, "密码至少6位，可使用英文字母、数字和符号"));
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            return ResponseEntity.badRequest().body(ApiResult.error(400, "密码过长，最多支持72字节"));
        }
        if (userService.existsByPhone(user.getPhone())) {
            return ResponseEntity.badRequest().body(ApiResult.error(400, "该手机号已注册"));
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setNickname(user.getNickname() == null ? "健身达人" : user.getNickname());
        user.setGender(user.getGender() == null ? 0 : user.getGender());
        return ResponseEntity.ok(ApiResult.success("注册成功", authData(userService.createUser(user))));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResult<?>> login(@RequestBody Map<String, String> loginData) {
        String phone = loginData.get("phone");
        String password = loginData.get("password");
        if (phone == null || password == null) {
            return ResponseEntity.badRequest().body(ApiResult.error(400, "请输入手机号和密码"));
        }
        Optional<User> user = userService.findByPhone(phone);
        if (user.isEmpty() || !passwordEncoder.matches(password, user.get().getPassword())) {
            return ResponseEntity.status(401).body(ApiResult.error(401, "手机号或密码错误"));
        }
        return ResponseEntity.ok(ApiResult.success("登录成功", authData(user.get())));
    }

    private Map<String, Object> authData(User user) {
        user.setPassword(null);
        Map<String, Object> result = new HashMap<>();
        result.put("token", jwtUtil.generateToken(user.getId(), user.getPhone()));
        result.put("userInfo", user);
        return result;
    }
}
