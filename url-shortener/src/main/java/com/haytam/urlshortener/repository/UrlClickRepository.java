package com.haytam.urlshortener.repository;

import com.haytam.urlshortener.entity.Url;
import com.haytam.urlshortener.entity.UrlClick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UrlClickRepository extends JpaRepository<UrlClick, Long> {

    long countByUrl(Url url);

    @Query("""
        SELECT COUNT(DISTINCT c.visitorId)
        FROM UrlClick c
        WHERE c.url = :url
    """)
    long countDistinctVisitorsByUrl(@Param("url") Url url);
}