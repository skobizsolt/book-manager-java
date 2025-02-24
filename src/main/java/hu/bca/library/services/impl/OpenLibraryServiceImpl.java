package hu.bca.library.services.impl;

import hu.bca.library.services.OpenLibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenLibraryServiceImpl implements OpenLibraryService {

    private final WebClient webClient;

    @Override
    public String getPublishDate(String workId) {
        final Map<?, ?> response = getBookResponse(workId);
        String publishDate =
                response == null
                        ? null
                        : (String) response.getOrDefault("first_publish_date", null);
        log.info("Response from OpenLibrary for workId: '{}' was {}", workId, publishDate);
        return publishDate;
    }

    private Map<?, ?> getBookResponse(String workId) {
        return webClient.get()
                .uri("/works/%s.json".formatted(workId))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .doOnError(throwable ->
                        log.error("No information found for book workId '{}'. Cause: {}",
                                workId,
                                throwable.getMessage()))
                .onErrorReturn(Collections.emptyMap())
                .block();
    }
}
