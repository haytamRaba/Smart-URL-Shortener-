package com.haytam.urlshortener.controller;

import com.haytam.urlshortener.entity.Url;
import com.haytam.urlshortener.service.ClickTrackingService;
import com.haytam.urlshortener.service.UrlService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@RestController
public class RedirectController {

    private static final String VISITOR_COOKIE = "url_visitor_id";

    private final UrlService urlService;
    private final ClickTrackingService clickTrackingService;

    public RedirectController(
            UrlService urlService,
            ClickTrackingService clickTrackingService
    ) {
        this.urlService = urlService;
        this.clickTrackingService = clickTrackingService;
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            @CookieValue(name = VISITOR_COOKIE, required = false) String visitorId
    ) {
        Url url = urlService.getByShortCode(shortCode);

        boolean newVisitor = false;

        if (visitorId == null || visitorId.isBlank()) {
            visitorId = UUID.randomUUID().toString();
            newVisitor = true;
        }

        clickTrackingService.trackClick(url, visitorId);

        ResponseEntity.BodyBuilder response = ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(url.getOriginalUrl()));

        if (newVisitor) {
            ResponseCookie cookie = ResponseCookie
                    .from(VISITOR_COOKIE, visitorId)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(Duration.ofDays(365))
                    .sameSite("Lax")
                    .build();

            response.header(
                    HttpHeaders.SET_COOKIE,
                    cookie.toString()
            );
        }

        return response.build();
    }
}