package com.haytam.urlshortener.controller;

import com.haytam.urlshortener.entity.Url;
import com.haytam.urlshortener.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class RedirectController {

    private final UrlService urlService;

    public RedirectController(UrlService urlService) {
        this.urlService = urlService;
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