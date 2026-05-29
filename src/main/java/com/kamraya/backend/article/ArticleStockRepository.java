package com.kamraya.backend.article;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ArticleStockRepository extends JpaRepository<ArticleStock, Long> {

    ArticleStock findByArticleIdAndColorAndSize(Long articleId, String color, String size);

    List<ArticleStock> findByArticleId(Long articleId);

    void deleteByArticleId(Long articleId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ArticleStock s WHERE s.articleId = :articleId AND s.color = :color AND s.size = :size")
    ArticleStock findByArticleIdAndColorAndSizeForUpdate(
        @Param("articleId") Long articleId,
        @Param("color") String color,
        @Param("size") String size
    );
}