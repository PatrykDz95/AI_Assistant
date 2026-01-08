package cdq.cdl.aiassistant.shared.config;

import java.time.Duration;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;

@Configuration
public class PgVectorStoreConfig
{
    @Bean
    public EmbeddingModel embeddingModel(
            @Value("${langchain4j.ollama.embedding-model.base-url:http://localhost:11434}") String baseUrl,
            @Value("${langchain4j.ollama.embedding-model.model-name:nomic-embed-text}") String modelName,
            @Value("${langchain4j.ollama.embedding-model.timeout:120s}") Duration timeout)
    {
        return OllamaEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .timeout(timeout)
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(DataSource dataSource)
    {
        return PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .table("cdq_product_knowledge")
                .dimension(768)
                .createTable(true)
                .dropTableFirst(false)
                .build();
    }

    @Bean
    public EmbeddingStoreIngestor embeddingStoreIngestor(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel)
    {
        return EmbeddingStoreIngestor.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .build();
    }
}

