package com.haytam.urlshortener.dto;

import java.time.Instant;

public record CreateUrlResponse(
        String originalUrl,
        String shortCode,
        String shortUrl,
        Instant createdAt
) {

}