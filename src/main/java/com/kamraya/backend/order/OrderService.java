package com.kamraya.backend.order;

import com.kamraya.backend.article.ArticleStockRepository;
import com.kamraya.backend.mail.BrevoMailService;
import com.kamraya.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ArticleStockRepository articleStockRepository;
    private final BrevoMailService brevoMailService;

    @Transactional
    public OrderDTO createOrder(OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("COMMANDE_VIDE");
        }
        if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            throw new RuntimeException("ADRESSE_VIDE");
        }

        var user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User non trouvé"));

        for (var i : request.getItems()) {
            var stock = articleStockRepository
                .findByArticleIdAndColorAndSizeForUpdate(i.getArticleId(), i.getColor(), i.getSize());
            if (stock == null || stock.getQuantity() < i.getQuantity()) {
                throw new RuntimeException("STOCK_INSUFFISANT:" + i.getName());
            }
        }

        Order order = Order.builder()
            .user(user)
            .address(request.getAddress())
            .total(request.getTotal())
            .build();

        List<OrderItem> items = request.getItems().stream().map(i ->
            OrderItem.builder()
                .order(order)
                .articleId(i.getArticleId())
                .name(i.getName())
                .price(i.getPrice())
                .color(i.getColor())
                .size(i.getSize())
                .quantity(i.getQuantity())
                .build()
        ).toList();

        order.setItems(items);
        Order saved = orderRepository.save(order);

        request.getItems().forEach(i -> {
            var stock = articleStockRepository
                .findByArticleIdAndColorAndSizeForUpdate(i.getArticleId(), i.getColor(), i.getSize());
            if (stock != null) {
                stock.setQuantity(Math.max(stock.getQuantity() - i.getQuantity(), 0));
                articleStockRepository.save(stock);
            }
        });

        sendOrderEmail(saved, user.getFullName(), user.getEmail(), user.getPhone());

        return toDTO(saved);
    }

    private String colorName(String hex) {
        return switch (hex) {
            case "#FFFFFF" -> "Blanc";
            case "#1a1a1a" -> "Noir";
            case "#8B6245" -> "Marron";
            case "#4a6741" -> "Vert";
            case "#C9A97A" -> "Beige";
            case "#c9a97a" -> "Beige";
            default -> hex;
        };
    }

    private void sendOrderEmail(Order order, String userName, String userEmail, String userPhone) {
        LocalDateTime now = LocalDateTime.now();
        String date = now.getDayOfMonth() + "/" + now.getMonthValue() + "/" + now.getYear();

        StringBuilder text = new StringBuilder();
        text.append("════════════════════════════════════\n");
        text.append("       NOUVELLE COMMANDE — KAMRAYA  \n");
        text.append("════════════════════════════════════\n\n");

        text.append("INFORMATIONS CLIENT\n");
        text.append("───────────────────\n");
        text.append("Nom       : ").append(userName).append("\n");
        text.append("Email     : ").append(userEmail).append("\n");
        text.append("Téléphone : ").append(userPhone).append("\n");
        text.append("Adresse   : ").append(order.getAddress()).append("\n");
        text.append("Date      : ").append(date).append("\n\n");

        text.append("ARTICLES COMMANDÉS\n");
        text.append("───────────────────\n");
        order.getItems().forEach(i -> {
            text.append("• ").append(i.getName()).append("\n");
            text.append("  Couleur    : ").append(colorName(i.getColor())).append("\n");
            text.append("  Taille     : ").append(i.getSize()).append("\n");
            text.append("  Quantité   : ").append(i.getQuantity()).append("\n");
            text.append("  Prix unit  : ").append(i.getPrice()).append(" TND\n");
            text.append("  Sous-total : ").append(i.getPrice() * i.getQuantity()).append(" TND\n\n");
        });

        text.append("───────────────────\n");
        text.append("TOTAL : ").append(order.getTotal()).append(" TND\n");
        text.append("════════════════════════════════════\n");

        brevoMailService.send(
            "nouranemesfar@gmail.com",
            "Nouvelle commande #" + order.getId() + " — " + userName + " — " + date,
            text.toString()
        );
    }

    public List<OrderDTO> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream().map(this::toDTO).toList();
    }

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
            .stream().map(this::toDTOWithUser).toList();
    }

    @Transactional
    public OrderDTO updateStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        order.setStatus(Order.OrderStatus.valueOf(status));
        return toDTOWithUser(orderRepository.save(order));
    }

    @Transactional
    public void deleteOrder(Long id) {
        orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        orderRepository.deleteById(id);
    }

    private OrderDTO toDTO(Order order) {
        return OrderDTO.builder()
            .id(order.getId())
            .address(order.getAddress())
            .total(order.getTotal())
            .status(order.getStatus().name())
            .createdAt(order.getCreatedAt())
            .items(order.getItems().stream().map(i ->
                OrderItemDTO.builder()
                    .articleId(i.getArticleId())
                    .name(i.getName())
                    .price(i.getPrice())
                    .color(i.getColor())
                    .size(i.getSize())
                    .quantity(i.getQuantity())
                    .build()
            ).toList())
            .build();
    }

    private OrderDTO toDTOWithUser(Order order) {
        return OrderDTO.builder()
            .id(order.getId())
            .address(order.getAddress())
            .total(order.getTotal())
            .status(order.getStatus().name())
            .createdAt(order.getCreatedAt())
            .userEmail(order.getUser().getEmail())
            .userName(order.getUser().getFullName())
            .userPhone(order.getUser().getPhone())
            .items(order.getItems().stream().map(i ->
                OrderItemDTO.builder()
                    .articleId(i.getArticleId())
                    .name(i.getName())
                    .price(i.getPrice())
                    .color(i.getColor())
                    .size(i.getSize())
                    .quantity(i.getQuantity())
                    .build()
            ).toList())
            .build();
    }
}