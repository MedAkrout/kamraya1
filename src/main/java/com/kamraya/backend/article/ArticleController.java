package com.kamraya.backend.article;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    // ── Routes publiques ────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<ArticleDTO>> getAllArticles() {
        return ResponseEntity.ok(articleService.getAllArticles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleDTO> getArticleById(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getArticleById(id));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<String> getStatus(
        @PathVariable Long id,
        @RequestParam String color,
        @RequestParam String size
    ) {
        return ResponseEntity.ok(articleService.getStatus(id, color, size));
    }

    // ── Routes ADMIN ────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    public ResponseEntity<ArticleDTO> createArticle(@RequestBody Article article) {
        return ResponseEntity.ok(articleService.createArticle(article));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    public ResponseEntity<ArticleDTO> updateArticle(
        @PathVariable Long id,
        @RequestBody Article article
    ) {
        return ResponseEntity.ok(articleService.updateArticle(id, article));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stocks")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    public ResponseEntity<List<ArticleStock>> getStocks(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getStocks(id));
    }

    @PutMapping("/{id}/stocks")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    public ResponseEntity<ArticleStock> updateStock(
        @PathVariable Long id,
        @RequestParam String color,
        @RequestParam String size,
        @RequestParam Integer quantity
    ) {
        return ResponseEntity.ok(articleService.updateStock(id, color, size, quantity));
    }

    @PostMapping("/upload-image")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws java.io.IOException {
        return ResponseEntity.ok(articleService.uploadImage(file));
    }
}