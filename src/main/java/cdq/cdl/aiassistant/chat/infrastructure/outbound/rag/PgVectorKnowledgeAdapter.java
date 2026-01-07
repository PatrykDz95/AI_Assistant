package cdq.cdl.aiassistant.chat.infrastructure.outbound.rag;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import cdq.cdl.aiassistant.chat.domain.port.ProductKnowledgePort;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class PgVectorKnowledgeAdapter implements ProductKnowledgePort
{
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    private static final int MAX_RESULTS = 10;
    private static final double MIN_SCORE = 0.1;

    @Override
    public String search(String query)
    {
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(MAX_RESULTS)
                .minScore(MIN_SCORE)
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();

        if (matches == null || matches.isEmpty())
        {
            return "No relevant information found";
        }

        return matches.stream()
                .map(m -> m.embedded().text())
                .collect(Collectors.joining("\n\n"));
    }
}
