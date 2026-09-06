package com.haytam.urlshortener.dto;

public record UrlStatisticsResponse(
        String shortCode,
        long totalClicks,
        long uniqueVisitors
) {
}
