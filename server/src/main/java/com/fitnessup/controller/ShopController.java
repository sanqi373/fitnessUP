package com.fitnessup.controller;

import com.fitnessup.common.ApiResult;
import com.fitnessup.service.WalletBalance;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shop")
public class ShopController {
    public record Product(int id, String name, int priceCents, String image) {}
    public record Line(long id, int productId, int quantity) {}
    public record Pick(@Positive long id, @Positive int quantity) {}
    public record Selection(@NotEmpty List<@NotNull @Valid Pick> items) {}
    private static final List<Product> PRODUCTS = List.of(
            new Product(1, "UP 专业瑜伽垫", 9900, "/products/yoga-mat.webp"),
            new Product(2, "弹力带套装", 3990, "/products/resistance-bands.webp"),
            new Product(3, "智能计数跳绳", 6900, "/products/counting-rope.webp"),
            new Product(4, "UP 运动水壶 750ml", 4900, "/products/water-bottle.webp"),
            new Product(5, "健身手套防滑款", 5900, "/products/training-gloves.webp"),
            new Product(6, "泡沫轴肌肉放松", 7900, "/products/foam-roller.webp"));
    private final JdbcClient jdbc;
    private final WalletBalance wallet;

    public ShopController(JdbcClient jdbc, WalletBalance wallet) {
        this.jdbc = jdbc;
        this.wallet = wallet;
    }

    private List<Line> lines(long userId) {
        return jdbc.sql("""
                SELECT id, product_id, quantity FROM shop_cart_line
                WHERE user_id=? AND paid_amount IS NULL ORDER BY id
                """).param(userId).query(Line.class).list();
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ApiResult<?> shop(@AuthenticationPrincipal Long userId) {
        return ApiResult.success(Map.of("products", PRODUCTS, "cart", lines(userId),
                "balance", wallet.available(userId).toPlainString()));
    }

    @PostMapping("/cart/items/{productId}")
    @Transactional
    public ApiResult<?> add(@AuthenticationPrincipal Long userId, @PathVariable int productId) {
        if (PRODUCTS.stream().noneMatch(p -> p.id() == productId))
            return ApiResult.error(404, "商品不存在");
        wallet.lockAccount(userId);
        jdbc.sql("""
                INSERT INTO shop_cart_line (user_id, product_id) VALUES (?, ?)
                ON DUPLICATE KEY UPDATE quantity=quantity+1
                """).params(userId, productId).update();
        return shop(userId);
    }

    @PostMapping("/cart/{operation:remove|purchase}")
    @Transactional
    public ApiResult<?> apply(@AuthenticationPrincipal Long userId, @PathVariable String operation,
                              @Valid @RequestBody Selection selection) {
        wallet.lockAccount(userId);
        var cart = lines(userId);
        var selected = cart.stream().filter(line -> selection.items().contains(new Pick(line.id(), line.quantity()))).toList();
        if (selected.size() != selection.items().size())
            return ApiResult.error(409, "购物车已更新，请重新选择");
        boolean purchase = operation.equals("purchase");
        if (purchase) {
            var total = selected.stream().map(this::cost).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (wallet.available(userId).compareTo(total) < 0)
                return ApiResult.error(409, "钱包余额不足，请先充值或调整所选商品");
        }
        for (Line line : selected) {
            if (purchase) {
                jdbc.sql("UPDATE shop_cart_line SET paid_amount=?, purchased_at=CURRENT_TIMESTAMP(3) WHERE user_id=? AND id=?")
                        .params(cost(line), userId, line.id()).update();
            } else {
                jdbc.sql("DELETE FROM shop_cart_line WHERE user_id=? AND id=?").params(userId, line.id()).update();
            }
        }
        return shop(userId);
    }

    private BigDecimal cost(Line line) {
        var product = PRODUCTS.stream().filter(p -> p.id() == line.productId()).findFirst().orElseThrow();
        return BigDecimal.valueOf((long) product.priceCents() * line.quantity(), 2);
    }
}
