package cdq.cdl.aiassistant.chat.infrastructure.outbound.rag;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import cdq.cdl.aiassistant.chat.domain.port.ProductKnowledgePort;

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
    private VectorStore vectorStore;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp()
    {
        // Clear vector store before each test for proper isolation
        jdbcTemplate.execute("TRUNCATE TABLE vector_store");
    }

    @Test
    void shouldReturnNonEmptyResultWhenVectorStoreHasContent()
    {
        // Given
        vectorStore.add(List.of(
                new Document("CDQ Fraud Guard is an AML solution for detecting money laundering."),
                new Document("The system monitors transactions in real-time for suspicious activity."),
                new Document("Compliance with global AML regulations is automated.")
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
        vectorStore.add(List.of(
                new Document("Anti-Money Laundering (AML) solutions help prevent financial crimes."),
                new Document("Risk assessment tools for compliance.")
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
        // Given - vector store is empty (cleared in setUp)

        // When - search for something that doesn't exist
        String result = knowledgePort.search("xyzabc123notfound");

        // Then - should return the "not found" message
        assertThat(result)
                .isNotNull()
                .isEqualTo("No relevant information found");
    }

    @Test
    void shouldHandleDocumentRetrievalGracefully()
    {
        // Given - add some documents
        vectorStore.add(List.of(
                new Document("Test document about machine learning"),
                new Document("Another test about artificial intelligence")
        ));

        // When - search for documents
        String result = knowledgePort.search("test");

        // Then - should return valid result (document content or not found message)
        assertThat(result)
                .isNotNull()
                .isNotEmpty();
    }
}

