package com.example.URLShortener.services;

import com.example.URLShortener.models.UrlEntity;
import com.example.URLShortener.repositories.ShortUrlRepository;
import com.example.URLShortener.util.UrlNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import static java.util.function.Predicate.not;

@Service
@Transactional(readOnly = true)
public class UrlService {
    private final ShortUrlRepository shortUrlRepository;

    @Autowired
    public UrlService(ShortUrlRepository shortUrlRepository) {
        this.shortUrlRepository = shortUrlRepository;
    }

    public List<UrlEntity> findAll() {
        return shortUrlRepository.findAll();
    }

    public UrlEntity findByShortUrl(String shortUrl) {
        Optional<UrlEntity> foundUrl = shortUrlRepository.findByShortUrl(shortUrl);
        return foundUrl.orElseThrow(UrlNotFoundException::new);
    }

    public Optional<UrlEntity> findByFullUrl(String fullUrl) {
        return shortUrlRepository.findByFullUrl(fullUrl);
    }

    @Transactional
    public UrlEntity save(final String fullUrl) {
        return Optional.ofNullable(fullUrl)
                .filter(not(String::isBlank))
                .map(this::generate)
                .map(shortUrlRepository::save)
                .orElseThrow(() -> new IllegalArgumentException("Not valid url"));
    }


    public UrlEntity generate(String fullUrl) {
        char[] map = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

        StringBuilder shortUrl = new StringBuilder();

        long n = getNextAvailableIndex();

        while (n > 0) {
            shortUrl.append(map[(int) (n % 62L)]);
            n = n / 62;
        }
        UrlEntity urlEntity = new UrlEntity();
        urlEntity.setShortUrl(shortUrl.toString());
        urlEntity.setFullUrl(fullUrl);
        urlEntity.setCreatedAt(LocalDateTime.now());

        return urlEntity;
    }

    private Long getNextAvailableIndex() {
        UrlEntity lastEntity = shortUrlRepository.findTopByOrderByIdDesc(); // Find the last entity
        if (lastEntity != null) {
            return lastEntity.getId() + 1; // Increment the last ID
        } else {
            return 1L; // Start from 1 if the table is empty
        }
    }
}
