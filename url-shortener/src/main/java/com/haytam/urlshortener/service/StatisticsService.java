package com.haytam.urlshortener.service;

import com.haytam.urlshortener.dto.DailyClickCount;
import com.haytam.urlshortener.dto.UrlStatisticsResponse;
import com.haytam.urlshortener.entity.Url;
import com.haytam.urlshortener.entity.UrlClick;
import com.haytam.urlshortener.repository.UrlClickRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

    private static final int CHART_WINDOW_DAYS = 30;

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

        Instant lastClickAt = urlClickRepository.findFirstByUrlOrderByClickedAtDesc(url)
                .map(UrlClick::getClickedAt)
                .orElse(null);

        List<DailyClickCount> dailyClicks = buildDailyClicks(url);

        return new UrlStatisticsResponse(
                url.getShortCode(),
                totalClicks,
                uniqueVisitors,
                url.getCreatedAt(),
                lastClickAt,
                dailyClicks
        );

    }

    /**
     * Builds a dense daily series for the last 30 days (UTC), filling
     * days without clicks with zero so the chart has no gaps.
     */
    private List<DailyClickCount> buildDailyClicks(Url url) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate fromDate = today.minusDays(CHART_WINDOW_DAYS - 1L);
        Instant from = fromDate.atStartOfDay(ZoneOffset.UTC).toInstant();

        List<Object[]> rows =
                urlClickRepository.countClicksPerDayByUrl(url, from);

        Map<LocalDate, Long> clicksByDay = new HashMap<>();
        for (Object[] row : rows) {
            LocalDate day = (LocalDate) row[0];
            Number clicks = (Number) row[1];
            clicksByDay.put(day, clicks.longValue());
        }

        List<DailyClickCount> dailyClicks = new ArrayList<>(CHART_WINDOW_DAYS);
        for (int i = 0; i < CHART_WINDOW_DAYS; i++) {
            LocalDate day = fromDate.plusDays(i);
            dailyClicks.add(new DailyClickCount(day, clicksByDay.getOrDefault(day, 0L)));
        }
        return dailyClicks;
    }
}