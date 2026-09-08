package com.haytam.urlshortener.dto;

import java.time.Instant;

public record ClickEvent(
        Long urlId,
        String visitorId,
        Instant clickedAt,
        String source
) {}