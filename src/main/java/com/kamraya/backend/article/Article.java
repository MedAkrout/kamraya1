package com.kamraya.backend.article;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "articles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String category;

    @ElementCollection
    @CollectionTable(name = "article_images", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "image_url")
    private List<String> imagesUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;  // ← valeur par défaut, jamais null

    @ElementCollection
    @CollectionTable(name = "article_colors", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "color")
    private List<String> colors;

    @ElementCollection
    @CollectionTable(name = "article_sizes", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "size")
    private List<String> sizes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.stock == null) this.stock = 0;  // ← sécurité supplémentaire
    }

    public String getBadge() {
        if (createdAt != null && createdAt.isAfter(LocalDateTime.now().minusMonths(1))) {
            return "Nouveau";
        }
        return null;
    }
}