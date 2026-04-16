package com.pinebook.api.service;

import com.pinebook.api.util.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Non-blocking HTTP client wrapping ALL calls to the Python AI service.
 *
 * Design rules:
 *  - Uses WebClient (reactive) for all calls
 *  - block() is called only inside async threads (processVideo) or
 *    inside the request thread after validation (search) — never stalls the NIO loop
 *  - Correlation ID is forwarded from MDC to every outbound request
 *  - On HTTP error: wraps and rethrows to let callers update DB status
 */
@Service
public class AIClientService {

    private static final Logger log = LoggerFactory.getLogger(AIClientService.class);

    private final WebClient aiServiceWebClient;

    @Value("${ai.service.timeout-ms:5000}")
    private long timeoutMs;

    public AIClientService(WebClient aiServiceWebClient) {
        this.aiServiceWebClient = aiServiceWebClient;
    }

    // ── /process-video ────────────────────────────────────────────────────────

    /**
     * Calls POST /process-video on the Python service.
     * Intended to be called from an async background thread only.
     * Blocks until the AI service responds (acceptable in async context).
     *
     * @param videoId 11-char YouTube video ID
     * @throws RuntimeException on non-2xx response or timeout
     */
    public void processVideo(String videoId) {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        log.info("→ AI /process-video videoId={} correlationId={}", videoId, correlationId);

        Map<String, String> body = Map.of(
                "video_id",       videoId,
                "correlation_id", correlationId != null ? correlationId : ""
        );

        try {
            Map<?, ?> response = aiServiceWebClient.post()
                    .uri("/process-video")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(CorrelationIdFilter.CORRELATION_ID_HEADER,
                            correlationId != null ? correlationId : "")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            log.info("← AI /process-video OK videoId={} response={}", videoId, response);
        } catch (WebClientResponseException e) {
            log.error("← AI /process-video HTTP {} videoId={} body={}",
                    e.getStatusCode(), videoId, e.getResponseBodyAsString());
            throw new RuntimeException(
                    "AI service /process-video failed [" + e.getStatusCode() + "]: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("← AI /process-video ERROR videoId={} error={}", videoId, e.getMessage(), e);
            throw new RuntimeException("AI service /process-video unreachable: " + e.getMessage(), e);
        }
    }

    // ── /search ───────────────────────────────────────────────────────────────

    /**
     * Calls POST /search on the Python service.
     * Returns a list of chunk result maps, each containing:
     *   { "text": "...", "timestamp": 120, "video_id": "abc", "score": 0.91 }
     *
     * Returns an empty list on failure — never propagates to controller.
     *
     * @param query         user's natural language query
     * @param correlationId tracing ID propagated from caller
     * @param topK          number of results to request
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> search(String query, String correlationId, int topK) {
        log.info("→ AI /search query=\"{}\" correlationId={}", query, correlationId);

        Map<String, Object> body = Map.of(
                "query",          query,
                "top_k",          topK,
                "correlation_id", correlationId != null ? correlationId : ""
        );

        try {
            Map<String, Object> response = aiServiceWebClient.post()
                    .uri("/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(CorrelationIdFilter.CORRELATION_ID_HEADER,
                            correlationId != null ? correlationId : "")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            List<Map<String, Object>> results =
                    (List<Map<String, Object>>) response.getOrDefault("results", List.of());

            log.info("← AI /search OK results={}", results.size());
            return results;

        } catch (WebClientResponseException e) {
            log.error("← AI /search HTTP {} query=\"{}\" body={}",
                    e.getStatusCode(), query, e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            log.error("← AI /search ERROR query=\"{}\" error={}", query, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Overload using default topK from config and MDC correlation ID.
     */
    public List<Map<String, Object>> search(String query) {
        return search(query, MDC.get(CorrelationIdFilter.MDC_KEY), 5);
    }
}
