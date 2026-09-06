package com.haytam.urlshortener.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "url_clicks")
public class UrlClick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "url_id", nullable = false)
    private Url url;

    @Column(nullable = false)
    private String visitorId;

    @CreationTimestamp
    @Column(nullable = false)
    private Instant clickedAt;

    protected UrlClick() {
    }

    public UrlClick(Url url, String visitorId) {
        this.url = url;
        this.visitorId = visitorId;
    }

    public Long getId() {
        return id;
    }

    public Url getUrl() {
        return url;
    }

    public String getVisitorId() {
        return visitorId;
    }

    public Instant getClickedAt() {
        return clickedAt;
    }
}