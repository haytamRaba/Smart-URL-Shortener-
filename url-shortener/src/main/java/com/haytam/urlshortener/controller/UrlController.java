package com.haytam.urlshortener.controller;

import com.haytam.urlshortener.dto.CreateUrlRequest;
import com.haytam.urlshortener.dto.CreateUrlResponse;
import com.haytam.urlshortener.entity.Url;
import com.haytam.urlshortener.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/urls")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping
    public CreateUrlResponse createUrl(
            @RequestBody CreateUrlRequest request
    ) {

        Url url = urlService.createShortUrl(request.originalUrl());

        String shortUrl = "http://localhost:8080/" + url.getShortCode();

        return new CreateUrlResponse(
                url.getOriginalUrl(),
                url.getShortCode(),
                shortUrl,
                url.getCreatedAt()
        );
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode
    ) {
        Url url = urlService.getByShortCode(shortCode);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(url.getOriginalUrl()))
                .build();
    }
}