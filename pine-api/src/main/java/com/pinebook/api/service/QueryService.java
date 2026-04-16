package com.pinebook.api.service;

import com.pinebook.api.dto.AskRequest;
import com.pinebook.api.dto.AskResponse;
import com.pinebook.api.dto.AskResponse.ChunkResult;
import com.pinebook.api.dto.AskResponse.LearningStep;
import com.pinebook.api.util.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Orchestrates the /ask pipeline:
 *
 *  1. LEARNING MODE — triggered when query contains "learn", "study", or "roadmap"
 *     Runs a search per ML topic and assembles a structured learning path.
 *
 *  2. BEST EXPLANATION SELECTOR — categorises results by content style:
 *     - "Example"   → chunk contains the word "example"
 *     - "Basics"    → short chunk (≤ 300 words)
 *     - "Deep Dive" → long chunk (> 300 words)
 *     Returns one best result per category.
 *
 *  3. CONFUSION RESOLVER — hardcoded concept pairs that learners commonly mix up.
 *     Runs a targeted search per pair and returns the best timestamp.
 *
 * Design rules:
 *  - No ML logic here; all AI is delegated to AIClientService → Python
 *  - Returns safe empty structures on any partial failure
 *  - Deterministic: same query → same categories → same ordering
 */
@Service
public class QueryService {

    private static final Logger log = LoggerFactory.getLogger(QueryService.class);

    private final AIClientService aiClientService;

    public QueryService(AIClientService aiClientService) {
        this.aiClientService = aiClientService;
    }

    // ── Learning mode topics (Machine Learning roadmap) ───────────────────────
    private static final List<String> ML_TOPICS = List.of(
            "machine learning basics",
            "types of machine learning",
            "machine learning algorithms",
            "machine learning applications"
    );

    private static final List<String> LEARNING_KEYWORDS = List.of("learn", "study", "roadmap");

    // ── Confusion concept pairs ───────────────────────────────────────────────
    private static final List<String> CONFUSION_PAIRS = List.of(
            "supervised vs unsupervised learning",
            "classification vs regression"
    );

    // ── Public entry point ────────────────────────────────────────────────────

    public AskResponse ask(AskRequest request) {
        String query         = request.query().trim();
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        log.info("QueryService.ask query=\"{}\" correlationId={}", query, correlationId);

        List<LearningStep>       learningPath     = buildLearningPath(query, correlationId);
        Map<String, ChunkResult> bestExplanations = selectBestExplanations(query, correlationId);
        List<ChunkResult>        confusions       = resolveConfusions(correlationId);

        return new AskResponse(learningPath, bestExplanations, confusions);
    }

    // ── 1. Learning mode ──────────────────────────────────────────────────────

    private List<LearningStep> buildLearningPath(String query, String correlationId) {
        boolean isLearningQuery = LEARNING_KEYWORDS.stream()
                .anyMatch(kw -> query.toLowerCase().contains(kw));

        if (!isLearningQuery) {
            log.debug("Not a learning query — skipping learning path");
            return List.of();
        }

        log.info("Learning mode activated for query=\"{}\"", query);
        List<LearningStep> steps = new ArrayList<>();

        for (String topic : ML_TOPICS) {
            List<Map<String, Object>> results =
                    aiClientService.search(topic, correlationId, 1);

            if (!results.isEmpty()) {
                ChunkResult best = toChunkResult(results.get(0));
                steps.add(new LearningStep(topic, best));
                log.debug("Learning step added topic=\"{}\" videoId={} timestamp={}",
                        topic, best.videoId(), best.timestamp());
            } else {
                log.warn("No results for learning topic=\"{}\"", topic);
            }
        }

        return Collections.unmodifiableList(steps);
    }

    // ── 2. Best explanation selector ──────────────────────────────────────────

    private Map<String, ChunkResult> selectBestExplanations(String query, String correlationId) {
        List<Map<String, Object>> results =
                aiClientService.search(query, correlationId, 10);

        if (results.isEmpty()) {
            log.warn("No results for best explanation selection query=\"{}\"", query);
            return Map.of();
        }

        Map<String, ChunkResult> explanations = new LinkedHashMap<>();

        for (Map<String, Object> raw : results) {
            String text = getString(raw, "text");
            ChunkResult chunk = toChunkResult(raw);

            if (!explanations.containsKey("Example")
                    && text.toLowerCase().contains("example")) {
                explanations.put("Example", chunk);
            }

            int wordCount = text.split("\\s+").length;
            if (!explanations.containsKey("Basics") && wordCount <= 150) {
                explanations.put("Basics", chunk);
            }
            if (!explanations.containsKey("Deep Dive") && wordCount > 150) {
                explanations.put("Deep Dive", chunk);
            }

            if (explanations.size() == 3) break; // all categories filled
        }

        log.info("Best explanations selected: {} categories", explanations.size());
        return Collections.unmodifiableMap(explanations);
    }

    // ── 3. Confusion resolver ─────────────────────────────────────────────────

    private List<ChunkResult> resolveConfusions(String correlationId) {
        List<ChunkResult> resolved = new ArrayList<>();

        for (String pair : CONFUSION_PAIRS) {
            List<Map<String, Object>> results =
                    aiClientService.search(pair, correlationId, 1);

            if (!results.isEmpty()) {
                ChunkResult best = toChunkResult(results.get(0));
                resolved.add(best);
                log.debug("Confusion resolved pair=\"{}\" videoId={} timestamp={}",
                        pair, best.videoId(), best.timestamp());
            } else {
                log.warn("No results for confusion pair=\"{}\"", pair);
            }
        }

        return Collections.unmodifiableList(resolved);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ChunkResult toChunkResult(Map<String, Object> raw) {
        return new ChunkResult(
                getString(raw, "text"),
                getInt(raw,    "timestamp"),
                getString(raw, "video_id")
        );
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private int getInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.intValue();
        try { return Integer.parseInt(val != null ? val.toString() : "0"); }
        catch (NumberFormatException e) { return 0; }
    }
}
