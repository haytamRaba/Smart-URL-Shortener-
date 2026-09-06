package com.haytam.urlshortener.service;

import com.haytam.urlshortener.entity.Url;
import com.haytam.urlshortener.entity.UrlClick;
import com.haytam.urlshortener.repository.UrlClickRepository;
import org.springframework.stereotype.Service;

@Service
public class ClickTrackingService {

    private final UrlClickRepository urlClickRepository;

    public ClickTrackingService(UrlClickRepository urlClickRepository) {
        this.urlClickRepository = urlClickRepository;
    }

    public void trackClick(Url url, String visitorId) {
        UrlClick click = new UrlClick(url, visitorId);

        urlClickRepository.save(click);
    }
}