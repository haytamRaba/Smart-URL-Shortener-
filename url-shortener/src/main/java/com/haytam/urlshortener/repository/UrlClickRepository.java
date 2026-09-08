package com.haytam.urlshortener.repository;

import com.haytam.urlshortener.entity.Url;
import com.haytam.urlshortener.entity.UrlClick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UrlClickRepository extends JpaRepository<UrlClick, Long> {

    long countByUrl(Url url);

    Optional<UrlClick> findFirstByUrlOrderByClickedAtDesc(Url url);

    @Query("""
        SELECT CAST(c.clickedAt AS LocalDate), COUNT(c)
        FROM UrlClick c
        WHERE c.url = :url
          AND c.clickedAt >= :from
        GROUP BY CAST(c.clickedAt AS LocalDate)
        ORDER BY CAST(c.clickedAt AS LocalDate)
    """)
    List<Object[]> countClicksPerDayByUrl(@Param("url") Url url, @Param("from") Instant from);

    @Query("""
        SELECT COUNT(DISTINCT c.visitorId)
        FROM UrlClick c
        WHERE c.url = :url
    """)
    long countDistinctVisitorsByUrl(@Param("url") Url url);
}