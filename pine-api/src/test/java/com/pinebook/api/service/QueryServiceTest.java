package com.pinebook.api.service;

import com.pinebook.api.dto.AskRequest;
import com.pinebook.api.dto.AskResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QueryService feature logic — no Spring context.
 * AIClientService is mocked; we test orchestration and categorization.
 */
class QueryServiceTest {

    private AIClientService aiClientService;
    private QueryService    queryService;

    // ── Fixture data ──────────────────────────────────────────────────────────

    private static Map<String, Object> chunk(String text, int timestamp, String videoId) {
        return Map.of("text", text, "timestamp", timestamp, "video_id", videoId, "score", 0.9);
    }

    private static final Map<String, Object> SHORT_CHUNK =
            chunk("an example of supervised learning classifies emails", 30, "vid001");

    private static final Map<String, Object> LONG_CHUNK =
            chunk("machine learning algorithms fall into several broad categories " +
                    "including supervised unsupervised and reinforcement learning " +
                    "each with its own set of techniques and applications that differ " +
                    "significantly in their approach to solving problems and extracting " +
                    "patterns from data sets of varying sizes and structures. " +
                    "Supervised learning uses labeled training data to learn a mapping function " +
                    "from inputs to outputs. Common supervised algorithms include linear regression " +
                    "logistic regression support vector machines decision trees random forests " +
                    "and neural networks. These algorithms are used for tasks such as " +
                    "classification regression and ranking. Unsupervised learning on the other hand " +
                    "operates on unlabeled data and attempts to discover hidden structure in the data. " +
                    "Clustering dimensionality reduction and generative modeling are the primary tasks " +
                    "in unsupervised learning. Popular algorithms include k-means clustering " +
                    "hierarchical clustering principal component analysis autoencoders and " +
                    "generative adversarial networks. Reinforcement learning is a paradigm " +
                    "where an agent learns to make decisions by interacting with an environment " +
                    "receiving rewards or penalties based on its actions. This approach has been " +
                    "successfully applied to game playing robotics autonomous vehicles and " +
                    "resource management problems. Deep learning a subset of machine learning " +
                    "uses multi-layered neural networks to learn hierarchical representations of data " +
                    "and has achieved state of the art results in computer vision natural language " +
                    "processing speech recognition and many other domains.", 60, "vid002");

    @BeforeEach
    void setUp() {
        aiClientService = mock(AIClientService.class);
        queryService    = new QueryService(aiClientService);
    }

    // ── Learning mode ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("ask: learning mode activates when query contains 'learn'")
    void ask_learningKeyword_populatesLearningPath() {
        when(aiClientService.search(anyString(), any(), anyInt()))
                .thenReturn(List.of(SHORT_CHUNK));

        AskResponse response = queryService.ask(new AskRequest("learn machine learning"));

        assertThat(response.learningPath()).isNotEmpty();
        assertThat(response.learningPath()).hasSize(4); // one per ML topic
        // Learning path topics should be labelled
        assertThat(response.learningPath().get(0).topic())
                .isEqualTo("machine learning basics");
    }

    @Test
    @DisplayName("ask: no learning mode when query has no trigger keywords")
    void ask_noLearningKeyword_emptyLearningPath() {
        when(aiClientService.search(anyString(), any(), anyInt()))
                .thenReturn(List.of(SHORT_CHUNK));

        AskResponse response = queryService.ask(new AskRequest("what is overfitting"));

        assertThat(response.learningPath()).isEmpty();
    }

    @Test
    @DisplayName("ask: 'roadmap' triggers learning mode")
    void ask_roadmapKeyword_activatesLearningMode() {
        when(aiClientService.search(anyString(), any(), anyInt()))
                .thenReturn(List.of(SHORT_CHUNK));

        AskResponse response = queryService.ask(new AskRequest("give me a roadmap for ML"));

        assertThat(response.learningPath()).isNotEmpty();
    }

    // ── Best explanation selector ─────────────────────────────────────────────

    @Test
    @DisplayName("ask: short chunk with 'example' yields Example and Basics categories")
    void ask_shortChunkWithExample_yieldsExampleAndBasics() {
        when(aiClientService.search(anyString(), any(), anyInt()))
                .thenReturn(List.of(SHORT_CHUNK));

        AskResponse response = queryService.ask(new AskRequest("what is machine learning"));

        assertThat(response.bestExplanations()).containsKey("Example");
        assertThat(response.bestExplanations()).containsKey("Basics");
    }

    @Test
    @DisplayName("ask: long chunk yields Deep Dive category")
    void ask_longChunk_yieldsDiveDive() {
        when(aiClientService.search(anyString(), any(), anyInt()))
                .thenReturn(List.of(LONG_CHUNK));

        AskResponse response = queryService.ask(new AskRequest("explain machine learning in depth"));

        assertThat(response.bestExplanations()).containsKey("Deep Dive");
    }

    @Test
    @DisplayName("ask: empty AI results → empty best explanations")
    void ask_emptyResults_emptyExplanations() {
        when(aiClientService.search(anyString(), any(), anyInt()))
                .thenReturn(List.of());

        AskResponse response = queryService.ask(new AskRequest("something obscure"));

        assertThat(response.bestExplanations()).isEmpty();
    }

    // ── Confusion resolver ────────────────────────────────────────────────────

    @Test
    @DisplayName("ask: confusion resolver always runs (2 hardcoded pairs)")
    void ask_confusionResolver_alwaysRuns() {
        when(aiClientService.search(anyString(), any(), anyInt()))
                .thenReturn(List.of(SHORT_CHUNK));

        AskResponse response = queryService.ask(new AskRequest("supervised learning"));

        // 2 hardcoded confusion pairs
        assertThat(response.confusions()).hasSize(2);
    }

    @Test
    @DisplayName("ask: confusion list is empty when AI returns no results")
    void ask_noResults_emptyConfusions() {
        when(aiClientService.search(anyString(), any(), anyInt()))
                .thenReturn(List.of());

        AskResponse response = queryService.ask(new AskRequest("what is ml"));

        assertThat(response.confusions()).isEmpty();
    }
}
