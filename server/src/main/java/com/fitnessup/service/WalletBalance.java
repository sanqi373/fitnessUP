package com.fitnessup.service;

import java.math.BigDecimal;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class WalletBalance {
    private final JdbcClient jdbc;

    public WalletBalance(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public BigDecimal available(long userId) {
        return jdbc.sql("""
                SELECT (SELECT COALESCE(SUM(amount), 0) FROM wallet_top_up WHERE user_id=?)
                     - (SELECT COALESCE(SUM(paid_amount), 0) FROM shop_cart_line WHERE user_id=?)
                """).params(userId, userId).query(BigDecimal.class).single().setScale(2);
    }

    public void lockAccount(long userId) {
        jdbc.sql("SELECT id FROM `user` WHERE id=? FOR UPDATE").param(userId).query(Long.class).single();
    }
}
