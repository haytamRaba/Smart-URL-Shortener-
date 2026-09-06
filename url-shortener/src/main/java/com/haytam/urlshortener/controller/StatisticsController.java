package com.haytam.urlshortener.controller;

import com.haytam.urlshortener.dto.UrlStatisticsResponse;
import com.haytam.urlshortener.service.StatisticsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/urls")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/{shortCode}/statistics")
    public UrlStatisticsResponse getStatistics(
            @PathVariable String shortCode
    ) {
        return statisticsService.getStatistics(shortCode);
    }
}