package com.haytam.urlshortener.service;

import com.haytam.urlshortener.entity.Url;
import com.haytam.urlshortener.repository.UrlRepository;
import org.springframework.stereotype.Service;

@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final ShortCodeGenerator shortCodeGenerator;

    public UrlService(
            UrlRepository urlRepository,
            ShortCodeGenerator shortCodeGenerator
    ) {
        this.urlRepository = urlRepository;
        this.shortCodeGenerator = shortCodeGenerator;
    }
    public Url createShortUrl(String originalUrl) {

        String shortCode ;
        do {
            shortCode = shortCodeGenerator.generate();
        } while (urlRepository.existsByShortCode(shortCode));


        Url url = new Url(originalUrl, shortCode);

        return urlRepository.save(url);
    }
    public Url getByShortCode(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Short URL not found"));
    }
}