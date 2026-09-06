package com.haytam.urlshortener.controller;

import com.haytam.urlshortener.dto.CreateUrlRequest;
import com.haytam.urlshortener.dto.CreateUrlResponse;
import com.haytam.urlshortener.entity.Url;
import com.haytam.urlshortener.service.UrlService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/urls")
public class UrlController {

    private final UrlService urlService;
    @Value("${app.base-url}")
    private String baseUrl;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping
    public CreateUrlResponse createUrl(
            @RequestBody CreateUrlRequest request
    ) {

        Url url = urlService.createShortUrl(request.originalUrl());

        String shortUrl = baseUrl + "/" + url.getShortCode();

        return new CreateUrlResponse(
                url.getOriginalUrl(),
                url.getShortCode(),
                shortUrl,
                url.getCreatedAt()
        );
    }


}