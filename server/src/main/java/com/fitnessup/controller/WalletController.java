package com.fitnessup.controller;

import com.fitnessup.common.ApiResult;
import com.fitnessup.service.WalletBalance;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/wallet")
public class WalletController {
    private final JdbcClient jdbc;
    private final WalletBalance wallet;

    public WalletController(JdbcClient jdbc, WalletBalance wallet) {
        this.jdbc = jdbc;
        this.wallet = wallet;
    }

    public record TopUp(@NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal amount) {}

    @GetMapping
    public ApiResult<?> balance(@AuthenticationPrincipal Long userId) {
        return ApiResult.success(Map.of("balance", wallet.available(userId).toPlainString()));
    }

    @PutMapping("/top-ups/{requestId:[0-9a-f]{32}}")
    @Transactional
    public ResponseEntity<ApiResult<?>> topUp(@AuthenticationPrincipal Long userId, @PathVariable String requestId,
                                             @Valid @RequestBody TopUp input) {
        wallet.lockAccount(userId);
        var previous = jdbc.sql("SELECT amount FROM wallet_top_up WHERE user_id=? AND request_id=?")
                .params(userId, requestId).query(BigDecimal.class).optional();
        if (previous.isEmpty()) {
            jdbc.sql("INSERT INTO wallet_top_up (user_id, request_id, amount) VALUES (?, ?, ?)")
                    .params(userId, requestId, input.amount()).update();
        } else if (previous.get().compareTo(input.amount()) != 0) {
            return ResponseEntity.status(409).body(ApiResult.error(409, "该笔充值的金额已确定"));
        }
        return ResponseEntity.ok(balance(userId));
    }
}
