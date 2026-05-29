package com.kamraya.backend.cart;

import com.kamraya.backend.article.ArticleRepository;
import com.kamraya.backend.article.ArticleStockRepository;
import com.kamraya.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ArticleRepository articleRepository;
    private final ArticleStockRepository articleStockRepository;
    private final UserRepository userRepository;

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart cart = Cart.builder()
                .user(userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User non trouvé")))
                .build();
            return cartRepository.save(cart);
        });
    }

    public List<CartItemDTO> getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return cart.getItems().stream()
            .map(this::toDTO)
            .filter(item -> item != null)
            .toList();
    }

    @Transactional
    public void addItem(Long userId, Long articleId, String color, String size) {
        Cart cart = getOrCreateCart(userId);

        var stock = articleStockRepository.findByArticleIdAndColorAndSize(articleId, color, size);
        int stockDispo = (stock != null) ? stock.getQuantity() : 0;

        cartItemRepository.findByCartIdAndArticleIdAndColorAndSize(
            cart.getId(), articleId, color, size
        ).ifPresentOrElse(
            item -> {
                if (item.getQuantity() < stockDispo) {
                    item.setQuantity(item.getQuantity() + 1);
                } else {
                    throw new RuntimeException("STOCK_MAX:" + stockDispo);
                }
            },
            () -> {
                if (stockDispo <= 0) {
                    throw new RuntimeException("STOCK_MAX:0");
                }
                CartItem item = CartItem.builder()
                    .cart(cart)
                    .articleId(articleId)
                    .color(color)
                    .size(size)
                    .quantity(1)
                    .build();
                cart.getItems().add(item);
            }
        );
        cartRepository.save(cart);
    }

    @Transactional
    public void increment(Long userId, Long articleId, String color, String size) {
        Cart cart = getOrCreateCart(userId);

        var stock = articleStockRepository.findByArticleIdAndColorAndSize(articleId, color, size);
        int stockDispo = (stock != null) ? stock.getQuantity() : 0;

        cartItemRepository.findByCartIdAndArticleIdAndColorAndSize(
            cart.getId(), articleId, color, size
        ).ifPresent(item -> {
            if (item.getQuantity() < stockDispo) {
                item.setQuantity(item.getQuantity() + 1);
            } else {
                throw new RuntimeException("STOCK_MAX:" + stockDispo);
            }
        });
        cartRepository.save(cart);
    }

    @Transactional
    public void decrement(Long userId, Long articleId, String color, String size) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.findByCartIdAndArticleIdAndColorAndSize(
            cart.getId(), articleId, color, size
        ).ifPresent(item -> {
            if (item.getQuantity() <= 1) {
                cart.getItems().remove(item);
            } else {
                item.setQuantity(item.getQuantity() - 1);
            }
        });
        cartRepository.save(cart);
    }

    @Transactional
    public void removeItem(Long userId, Long articleId, String color, String size) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().removeIf(item ->
            item.getArticleId().equals(articleId) &&
            item.getColor().equals(color) &&
            item.getSize().equals(size)
        );
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private CartItemDTO toDTO(CartItem item) {
        return articleRepository.findById(item.getArticleId()).map(article -> {
            var stock = articleStockRepository
                .findByArticleIdAndColorAndSize(item.getArticleId(), item.getColor(), item.getSize());
            int stockDispo = (stock != null) ? stock.getQuantity() : 0;
            String status = stockDispo > 0 ? "on sale" : "out of stock";

            return CartItemDTO.builder()
                .articleId(item.getArticleId())
                .name(article.getName())
                .price(article.getPrice())
                .category(article.getCategory())
                .color(item.getColor())
                .size(item.getSize())
                .quantity(item.getQuantity())
                .status(status)
                .maxQuantity(stockDispo)
                .build();
        }).orElse(null);
    }
}