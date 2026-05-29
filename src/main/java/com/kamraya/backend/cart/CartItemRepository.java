package com.kamraya.backend.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndArticleIdAndColorAndSize(
        Long cartId, Long articleId, String color, String size
    );
    void deleteByCartIdAndArticleIdAndColorAndSize(
        Long cartId, Long articleId, String color, String size
    );
}