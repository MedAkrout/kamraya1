package com.kamraya.backend.article;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "article_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private String size;

    @Column(nullable = false)
    private Integer quantity;
}