package cdq.cdl.aiassistant.shared.config;

import java.time.Duration;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;

/**
 * - LangChain4j handles Ollama communication (chat + embeddings)
 * - Spring AI handles PgVector storage
 */
@Configuration
public class EmbeddingBridgeConfig
{
    @Bean
    public EmbeddingModel langchain4jEmbeddingModel(
            @Value("${langchain4j.ollama.embedding-model.base-url:http://localhost:11434}") String baseUrl,
            @Value("${langchain4j.ollama.embedding-model.model-name:nomic-embed-text}") String modelName,
            @Value("${langchain4j.ollama.embedding-model.timeout:60s}") Duration timeout)
    {
        return OllamaEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .timeout(timeout)
                .build();
    }

    /**
     * Adapter that bridges LangChain4j EmbeddingModel to Spring AI's EmbeddingModel interface.
     * This allows PgVectorStore to use LangChain4j's Ollama embeddings.
     */
    @Bean
    public org.springframework.ai.embedding.EmbeddingModel springAiEmbeddingModel(
            EmbeddingModel langchain4jEmbeddingModel)
    {
        return new org.springframework.ai.embedding.EmbeddingModel()
        {
            @Override
            public EmbeddingResponse call(EmbeddingRequest request)
            {
                List<String> texts = request.getInstructions();

                // Convert strings to TextSegments for LangChain4j
                List<dev.langchain4j.data.segment.TextSegment> segments = texts.stream()
                        .map(dev.langchain4j.data.segment.TextSegment::from)
                        .toList();

                // Use LangChain4j to get embeddings
                List<Embedding> embeddings = langchain4jEmbeddingModel.embedAll(segments).content();

                // Convert LangChain4j Embeddings to Spring AI Embeddings
                List<org.springframework.ai.embedding.Embedding> springAiEmbeddings = embeddings.stream()
                        .map(lc4jEmbedding -> {
                            List<Float> vectorList = lc4jEmbedding.vectorAsList();
                            float[] vector = new float[vectorList.size()];
                            for (int i = 0; i < vectorList.size(); i++)
                            {
                                vector[i] = vectorList.get(i);
                            }
                            return new org.springframework.ai.embedding.Embedding(vector, 0);
                        })
                        .toList();

                return new EmbeddingResponse(springAiEmbeddings);
            }

            @Override
            public float[] embed(Document document)
            {
                String text = document.getText();
                Embedding embedding = langchain4jEmbeddingModel.embed(text).content();
                List<Float> vector = embedding.vectorAsList();
                float[] result = new float[vector.size()];
                for (int i = 0; i < vector.size(); i++)
                {
                    result[i] = vector.get(i);
                }
                return result;
            }
        };
    }
}

