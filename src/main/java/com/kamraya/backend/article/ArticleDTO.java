package com.kamraya.backend.article;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ArticleDTO {
    private Long id;
    private String name;
    private Double price;
    private String category;
    private List<String> imagesUrl;
    private String description;
    private String badge;
    private String status;
    private List<String> colors;
    private List<String> sizes;
    private List<ArticleStock> stocks;  // ✅ stock par couleur+taille (admin uniquement)
}