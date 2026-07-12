package com.kamraya.backend.article;

import com.kamraya.backend.mail.BrevoMailService;
import com.kamraya.backend.user.User;
import com.kamraya.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleStockRepository articleStockRepository;
    private final UserRepository userRepository;
    private final BrevoMailService brevoMailService;

    public List<ArticleDTO> getAllArticles() {
        return articleRepository.findAll()
            .stream()
            .map(this::toDTO)
            .toList();
    }

    public ArticleDTO getArticleById(Long id) {
        Article article = articleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Article non trouvé"));
        return toDTO(article);
    }

    public String getStatus(Long id, String color, String size) {
        ArticleStock stock = articleStockRepository
            .findByArticleIdAndColorAndSize(id, color, size);
        if (stock == null || stock.getQuantity() <= 0) {
            return "out of stock";
        }
        return "on sale";
    }

    @Transactional
    public ArticleDTO createArticle(Article article) {
        Article saved = articleRepository.save(article);

        for (String color : saved.getColors()) {
            for (String size : saved.getSizes()) {
                ArticleStock stock = ArticleStock.builder()
                    .articleId(saved.getId())
                    .color(color)
                    .size(size)
                    .quantity(0)
                    .build();
                articleStockRepository.save(stock);
            }
        }

        sendNewProductEmail(saved);

        return toDTOWithStock(saved);
    }

    @Transactional
    public ArticleDTO updateArticle(Long id, Article updated) {
        Article article = articleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Article non trouvé"));

        article.setName(updated.getName());
        article.setDescription(updated.getDescription());
        article.setPrice(updated.getPrice());
        article.setCategory(updated.getCategory());
        article.setColors(updated.getColors());
        article.setSizes(updated.getSizes());
        article.setImagesUrl(updated.getImagesUrl());

        Article saved = articleRepository.save(article);

        for (String color : saved.getColors()) {
            for (String size : saved.getSizes()) {
                ArticleStock existing = articleStockRepository
                    .findByArticleIdAndColorAndSize(saved.getId(), color, size);
                if (existing == null) {
                    articleStockRepository.save(ArticleStock.builder()
                        .articleId(saved.getId())
                        .color(color)
                        .size(size)
                        .quantity(0)
                        .build());
                }
            }
        }

        return toDTOWithStock(saved);
    }

    @Transactional
    public void deleteArticle(Long id) {
        articleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Article non trouvé"));
        articleStockRepository.deleteByArticleId(id);
        articleRepository.deleteById(id);
    }

    @Transactional
    public ArticleStock updateStock(Long articleId, String color, String size, Integer quantity) {
        ArticleStock stock = articleStockRepository
            .findByArticleIdAndColorAndSize(articleId, color, size);
        if (stock == null) {
            stock = ArticleStock.builder()
                .articleId(articleId)
                .color(color)
                .size(size)
                .quantity(quantity)
                .build();
        } else {
            stock.setQuantity(quantity);
        }
        return articleStockRepository.save(stock);
    }

    public List<ArticleStock> getStocks(Long articleId) {
        return articleStockRepository.findByArticleId(articleId);
    }

    @Async
    public void sendNewProductEmail(Article article) {
        List<User> users = userRepository.findByRole(User.Role.USER);
        if (users.isEmpty()) return;

        String[] emails = users.stream()
            .map(User::getEmail)
            .toArray(String[]::new);

        StringBuilder text = new StringBuilder();
        text.append("════════════════════════════════════\n");
        text.append("       NOUVEAU PRODUIT — KAMRAYA    \n");
        text.append("════════════════════════════════════\n\n");
        text.append("Bonjour,\n\n");
        text.append("Un nouveau produit vient d'être ajouté à notre boutique !\n\n");
        text.append("DÉTAILS DU PRODUIT\n");
        text.append("───────────────────\n");
        text.append("Nom         : ").append(article.getName()).append("\n");
        text.append("Catégorie   : ").append(article.getCategory()).append("\n");
        text.append("Prix        : ").append(article.getPrice()).append(" TND\n");
        if (article.getDescription() != null && !article.getDescription().isEmpty()) {
            text.append("Description : ").append(article.getDescription()).append("\n");
        }
        text.append("\n");
        text.append("Découvrez-le sur notre boutique : http://localhost:4200\n\n");
        text.append("════════════════════════════════════\n");
        text.append("L'équipe Kamraya\n");

        brevoMailService.send(
            emails,
            "🆕 Nouveau produit disponible — " + article.getName() + " | Kamraya",
            text.toString(),
            null
        );
    }

    private ArticleDTO toDTO(Article article) {
        boolean hasStock = article.getColors().stream().anyMatch(color ->
            article.getSizes().stream().anyMatch(size -> {
                ArticleStock stock = articleStockRepository
                    .findByArticleIdAndColorAndSize(article.getId(), color, size);
                return stock != null && stock.getQuantity() > 0;
            })
        );

        return ArticleDTO.builder()
            .id(article.getId())
            .name(article.getName())
            .price(article.getPrice())
            .category(article.getCategory())
            .imagesUrl(article.getImagesUrl())
            .description(article.getDescription())
            .badge(article.getBadge())
            .status(hasStock ? "on sale" : "out of stock")
            .colors(article.getColors())
            .sizes(article.getSizes())
            .build();
    }

    private ArticleDTO toDTOWithStock(Article article) {
        List<ArticleStock> stocks = articleStockRepository.findByArticleId(article.getId());
        boolean hasStock = stocks.stream().anyMatch(s -> s.getQuantity() > 0);

        return ArticleDTO.builder()
            .id(article.getId())
            .name(article.getName())
            .price(article.getPrice())
            .category(article.getCategory())
            .imagesUrl(article.getImagesUrl())
            .description(article.getDescription())
            .badge(article.getBadge())
            .status(hasStock ? "on sale" : "out of stock")
            .colors(article.getColors())
            .sizes(article.getSizes())
            .stocks(stocks)
            .build();
    }

    public String uploadImage(MultipartFile file) throws FileNotFoundException, java.io.IOException {
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        java.io.File uploadDir = new java.io.File(System.getProperty("user.dir") + "/uploads");
        if (!uploadDir.exists()) uploadDir.mkdirs();
        java.io.File dest = new java.io.File(uploadDir, filename);
        try (java.io.OutputStream os = new java.io.FileOutputStream(dest)) {
            os.write(file.getBytes());
        }
        return "/uploads/" + filename;
    }
}