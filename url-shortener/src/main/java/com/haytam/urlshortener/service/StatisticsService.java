package com.haytam.urlshortener.service;

import com.haytam.urlshortener.dto.UrlStatisticsResponse;
import com.haytam.urlshortener.entity.Url;
import com.haytam.urlshortener.repository.UrlClickRepository;
import org.springframework.stereotype.Service;

@Service
public class StatisticsService {

    private final UrlService urlService;
    private final UrlClickRepository urlClickRepository;

    public StatisticsService(
            UrlService urlService,
            UrlClickRepository urlClickRepository
    ) {
        this.urlService = urlService;
        this.urlClickRepository = urlClickRepository;
    }

    public UrlStatisticsResponse getStatistics(String shortCode) {

        Url url = urlService.getByShortCode(shortCode);

        long totalClicks =
                urlClickRepository.countByUrl(url);

        long uniqueVisitors =
                urlClickRepository.countDistinctVisitorsByUrl(url);

        return new UrlStatisticsResponse(
                url.getShortCode(),
                totalClicks,
                uniqueVisitors
        );

    }
}