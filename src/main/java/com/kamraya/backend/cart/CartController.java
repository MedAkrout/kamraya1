package com.kamraya.backend.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<CartItemDTO>> getCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<?> addItem(
        @PathVariable Long userId,
        @RequestParam Long articleId,
        @RequestParam String color,
        @RequestParam String size
    ) {
        try {
            cartService.addItem(userId, articleId, color, size);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("STOCK_MAX")) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            throw e;
        }
    }

    @PutMapping("/{userId}/increment")
    public ResponseEntity<?> increment(
        @PathVariable Long userId,
        @RequestParam Long articleId,
        @RequestParam String color,
        @RequestParam String size
    ) {
        try {
            cartService.increment(userId, articleId, color, size);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("STOCK_MAX")) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            throw e;
        }
    }

    @PutMapping("/{userId}/decrement")
    public ResponseEntity<Void> decrement(
        @PathVariable Long userId,
        @RequestParam Long articleId,
        @RequestParam String color,
        @RequestParam String size
    ) {
        cartService.decrement(userId, articleId, color, size);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/remove")
    public ResponseEntity<Void> removeItem(
        @PathVariable Long userId,
        @RequestParam Long articleId,
        @RequestParam String color,
        @RequestParam String size
    ) {
        cartService.removeItem(userId, articleId, color, size);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }
}