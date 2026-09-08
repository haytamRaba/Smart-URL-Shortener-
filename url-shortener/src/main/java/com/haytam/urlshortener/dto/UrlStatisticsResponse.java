package com.haytam.urlshortener.dto;

import java.time.Instant;
import java.util.List;

public record UrlStatisticsResponse(
        String shortCode,
        long totalClicks,
        long uniqueVisitors,
        Instant createdAt,
        Instant lastClickAt,
        List<DailyClickCount> dailyClicks
) {
}
