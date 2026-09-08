package com.haytam.urlshortener.dto;

import java.time.LocalDate;

public record DailyClickCount(
        LocalDate date,
        long clicks
) {
}
