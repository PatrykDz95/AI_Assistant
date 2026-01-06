package cdq.cdl.aiassistant.chat.infrastructure.outbound.rag;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import cdq.cdl.aiassistant.chat.domain.port.ProductKnowledgePort;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class PgVectorKnowledgeAdapter implements ProductKnowledgePort
{

    private final VectorStore vectorStore;

    @Override
    public String search(String query)
    {

        List<Document> docs = vectorStore.similaritySearch(query);

        if (docs == null || docs.isEmpty())
        {
            return "No relevant information found";
        }

        return docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));
    }
}
