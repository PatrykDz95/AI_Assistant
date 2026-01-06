package cdq.cdl.aiassistant.chat.infrastructure.outbound.rag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
class CdqFraudGuardIngestionJob
{
    private static final String CDQ_FRAUD_GUARD_URL = "https://www.cdq.com/products/cdq-fraud-guard";

    private final VectorStore vectorStore;

    @PostConstruct
    void ingest()
    {
        log.info("Starting CDQ Fraud Guard content ingestion from [{}]", CDQ_FRAUD_GUARD_URL);

        try
        {
            Document doc = Jsoup.connect(CDQ_FRAUD_GUARD_URL)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)")
                    .timeout(10000)
                    .get();

            List<org.springframework.ai.document.Document> documents = new ArrayList<>();

            // Extract title and meta description
            String title = doc.title();
            String metaDescription = doc.select("meta[name=description]").attr("content");

            if (!title.isEmpty() && !metaDescription.isEmpty())
            {
                documents.add(new org.springframework.ai.document.Document(
                        title + "\n\n" + metaDescription
                ));
                log.debug("Extracted title: [{}]", title);
            }

            // Extract main content paragraphs
            Elements paragraphs = doc.select("p");
            StringBuilder contentBuilder = new StringBuilder();

            for (Element p : paragraphs)
            {
                String text = p.text().trim();
                // Filter out short/empty paragraphs and navigation text
                if (text.length() > 50 && !text.toLowerCase().contains("cookie"))
                {
                    contentBuilder.append(text).append("\n\n");
                }
            }

            // Split into chunks (max 500 chars per chunk for better retrieval)
            String fullContent = contentBuilder.toString();
            if (!fullContent.isEmpty())
            {
                List<String> chunks = splitIntoChunks(fullContent);
                for (String chunk : chunks)
                {
                    documents.add(new org.springframework.ai.document.Document(chunk));
                }
                log.debug("Extracted [{}] content chunks", chunks.size());
            }

            // Extract headings with context
            Elements headings = doc.select("h1, h2, h3");
            for (Element heading : headings)
            {
                String headingText = heading.text().trim();
                if (headingText.length() > 10)
                {
                    Element nextElement = heading.nextElementSibling();
                    String context = nextElement != null ? nextElement.text() : "";

                    String combined = headingText + "\n" + context;
                    if (combined.length() > 20)
                    {
                        documents.add(new org.springframework.ai.document.Document(combined));
                    }
                }
            }
            if (documents.isEmpty())
            {
                log.warn("No content extracted from website");
            }

            vectorStore.add(documents);

            log.info("Successfully ingested {} CDQ Fraud Guard documents into vector store", documents.size());

        }
        catch (IOException e)
        {
            log.error("Failed to scrape CDQ Fraud Guard page: {}. Using fallback content.", e.getMessage());

            // Fallback: use static content
            vectorStore.add(List.of(new org.springframework.ai.document.Document("""
                    CDQ Fraud Guard is an AML (Anti-Money Laundering) solution that helps financial institutions
                    detect money laundering risks, monitor transactions, and ensure compliance with global regulations.
                    
                    Key features include real-time transaction monitoring, risk assessment, regulatory compliance automation,
                    and machine learning-based fraud detection.
                    """)));
        }
    }

    private List<String> splitIntoChunks(String text)
    {
        List<String> chunks = new ArrayList<>();
        String[] sentences = text.split("\\. ");

        StringBuilder currentChunk = new StringBuilder();
        for (String sentence : sentences)
        {
            if (currentChunk.length() + sentence.length() > 500 && !currentChunk.isEmpty())
            {
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder();
            }
            currentChunk.append(sentence).append(". ");
        }

        if (!currentChunk.isEmpty())
        {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }
}