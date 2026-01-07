package cdq.cdl.aiassistant.chat.infrastructure.outbound.rag;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import cdq.cdl.aiassistant.chat.domain.port.ProductKnowledgePort;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class PgVectorProductKnowledgeAdapterIT
{
    @Autowired
    private ProductKnowledgePort knowledgePort;

    @Autowired
    private EmbeddingStoreIngestor ingestor;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp()
    {
        jdbcTemplate.execute("TRUNCATE TABLE cdq_product_knowledge");
    }

    @Test
    void shouldReturnNonEmptyResultWhenVectorStoreHasContent()
    {
        // Given
        ingestor.ingest(List.of(
                Document.from("CDQ Fraud Guard is an AML solution for detecting money laundering."),
                Document.from("The system monitors transactions in real-time for suspicious activity."),
                Document.from("Compliance with global AML regulations is automated.")
        ));

        // When
        String result = knowledgePort.search("fraud detection");

        // Then
        assertThat(result)
                .isNotEmpty();
    }

    @Test
    void shouldReturnValidResultWhenSearching()
    {
        // Given
        ingestor.ingest(List.of(
                Document.from("Anti-Money Laundering (AML) solutions help prevent financial crimes."),
                Document.from("Risk assessment tools for compliance.")
        ));

        // When
        String result = knowledgePort.search("compliance");

        // Then
        assertThat(result)
                .isNotEmpty()
                .isNotBlank();
    }

    @Test
    void shouldHandleEmptySearchResults()
    {
        // Given vector store is empty

        // When search for something that doesn't exist
        String result = knowledgePort.search("xyzabc123notfound");

        // Then
        assertThat(result)
                .isNotNull()
                .isEqualTo("No relevant information found");
    }

    @Test
    void shouldHandleDocumentRetrievalGracefully()
    {
        // Given
        ingestor.ingest(List.of(
                Document.from("Test document about machine learning"),
                Document.from("Another test about artificial intelligence")
        ));

        // When
        String result = knowledgePort.search("test");

        // Then should return valid result
        assertThat(result)
                .isNotNull()
                .isNotEmpty();
    }
}

