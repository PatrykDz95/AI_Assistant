package cdq.cdl.aiassistant.chat.infrastructure.outbound.rag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
class CdqFraudGuardIngestionJob
{
    @Value("${rag.ingestion.cdq-fraud-guard.url}")
    private String fraudGuardUrl;

    private final EmbeddingStoreIngestor ingestor;
    private final PgVectorIngestionHelper ingestionHelper;

    @PostConstruct
    void ingest()
    {
        if (ingestionHelper.tableExistsAndNotEmpty())
        {
            log.info("Vector table already exists and is not empty, skipping ingestion");
            return;
        }

        log.info("Starting CDQ Fraud Guard content ingestion from [{}]", fraudGuardUrl);

        try
        {
            org.jsoup.nodes.Document doc = Jsoup.connect(fraudGuardUrl)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)")
                    .timeout(10000)
                    .get();

            List<String> textChunks = new ArrayList<>();

            // Extract title and meta description
            String title = doc.title();
            String metaDescription = doc.select("meta[name=description]").attr("content");

            if (!title.isEmpty() && !metaDescription.isEmpty())
            {
                textChunks.add("OVERVIEW: " + title + "\n\n" + metaDescription);
                log.debug("Extracted title: [{}]", title);
            }

            // Remove header, navigation, footer, and other non-content elements
            doc.select("header, nav, footer, .header, .navigation, .nav, .menu, .footer").remove();
            doc.select("[role=navigation], [role=banner], [role=contentinfo]").remove();
            doc.select(".cookie, .cookies, #cookie-banner, .gdpr").remove();

            // Target main content areas only
            Elements mainContent = doc.select("main, article, .content, .main-content, [role=main]");

            if (mainContent.isEmpty())
            {
                // Fallback but still exclude common non-content areas
                mainContent = doc.select("body");
                mainContent.select("header, nav, footer, aside, .sidebar").remove();
                log.debug("Fallback to body element for content extraction (with filters)");
            }

            // Extract structured sections with headings + paragraphs (exclude nav-related sections)
            Elements sections = mainContent.select("section:not([class*='nav']):not([class*='menu']), div[class*='section']:not([class*='nav']), div[class*='feature'], div[class*='block']");

            for (Element section : sections)
            {
                String sectionText = extractSectionContent(section);
                if (sectionText.length() > 100)
                {
                    textChunks.add(sectionText);
                }
            }

            // Fallback: extract all meaningful paragraphs if sections didn't work well
            if (textChunks.size() < 5)
            {
                log.debug("Section extraction yielded few results, using paragraph fallback");
                Elements paragraphs = mainContent.select("p");
                StringBuilder content = new StringBuilder();

                for (Element p : paragraphs)
                {
                    String text = p.text().trim();
                    // More lenient filtering
                    if (text.length() > 30 && isRelevantContent(text))
                    {
                        content.append(text).append("\n\n");
                    }
                }

                if (!content.isEmpty())
                {
                    textChunks.addAll(splitIntoChunks(content.toString()));
                }
            }

            // Extract lists (often contain key features and benefits)
            Elements lists = mainContent.select("ul, ol");
            for (Element list : lists)
            {
                StringBuilder listContent = new StringBuilder("KEY POINTS:\n");
                Elements items = list.select("li");

                for (Element item : items)
                {
                    String itemText = item.text().trim();
                    if (itemText.length() > 10 && isRelevantContent(itemText))
                    {
                        listContent.append("• ").append(itemText).append("\n");
                    }
                }

                if (listContent.length() > 50)
                {
                    textChunks.add(listContent.toString());
                }
            }

            // Extract all headings with their full context
            Elements headings = mainContent.select("h1, h2, h3, h4");
            for (Element heading : headings)
            {
                String headingText = heading.text().trim();
                if (headingText.length() > 10 && isRelevantContent(headingText))
                {
                    StringBuilder headingContext = new StringBuilder("SECTION: ")
                            .append(headingText).append("\n\n");

                    // Get all following siblings until next heading
                    Element sibling = heading.nextElementSibling();
                    int siblingCount = 0;

                    while (sibling != null && siblingCount < 5)
                    {
                        if (sibling.tagName().matches("h[1-6]"))
                        {
                            break;
                        }

                        String siblingText = sibling.text().trim();
                        if (siblingText.length() > 20 && isRelevantContent(siblingText))
                        {
                            headingContext.append(siblingText).append("\n");
                        }

                        sibling = sibling.nextElementSibling();
                        siblingCount++;
                    }

                    if (headingContext.length() > 50)
                    {
                        textChunks.add(headingContext.toString());
                    }
                }
            }

            if (textChunks.isEmpty())
            {
                log.warn("No content extracted from website");
            }

            List<String> uniqueChunks = textChunks.stream()
                    .map(String::trim)
                    .filter(s -> s.length() > 50)
                    .distinct()
                    .toList();

            List<Document> documents = uniqueChunks.stream()
                    .map(Document::from)
                    .toList();

            ingestor.ingest(documents);

            log.info("Successfully ingested {} CDQ Fraud Guard documents into vector store", documents.size());

        }
        catch (IOException e)
        {
            log.error("Failed to scrape CDQ Fraud Guard page: {}. Using fallback content.", e.getMessage());

            Document fallbackDoc = Document.from("""
                    CDQ Fraud Guard is an AML (Anti-Money Laundering) solution that helps financial institutions
                    detect money laundering risks, monitor transactions, and ensure compliance with global regulations.
                    
                    Key features include real-time transaction monitoring, risk assessment, regulatory compliance automation,
                    and machine learning-based fraud detection.
                    """);
            ingestor.ingest(fallbackDoc);
        }
    }

    private String extractSectionContent(Element section)
    {
        StringBuilder content = new StringBuilder();

        // Get heading
        Element heading = section.selectFirst("h1, h2, h3, h4");
        if (heading != null)
        {
            content.append("SECTION: ").append(heading.text()).append("\n\n");
        }

        // Get all paragraphs in this section
        Elements paragraphs = section.select("p");
        for (Element p : paragraphs)
        {
            String text = p.text().trim();
            if (text.length() > 20 && isRelevantContent(text))
            {
                content.append(text).append("\n");
            }
        }

        return content.toString().trim();
    }

    private boolean isRelevantContent(String text)
    {
        String lower = text.toLowerCase();

        // Filter out navigation, menu, and boilerplate text
        return !lower.contains("cookie")
                && !lower.contains("accept all")
                && !lower.contains("privacy policy")
                && !lower.contains("terms of service")
                && !lower.contains("© 20")
                && !lower.contains("all rights reserved")
                && !lower.contains("sign in")
                && !lower.contains("log in")
                && !lower.contains("login")
                && !lower.contains("register")
                && !lower.contains("subscribe")
                && !lower.contains("newsletter")
                && !lower.contains("contact us")
                && !lower.contains("get in touch")
                && !lower.contains("request demo")
                && !lower.contains("follow us")
                && !lower.contains("social media")
                && !lower.matches("^(home|about|about us|contact|products|services|solutions|careers|blog|company|resources|support|platform|news|events|industries)$")
                && !lower.matches(".*\\(opens in new (window|tab)\\).*");
    }

    private List<String> splitIntoChunks(String text)
    {
        List<String> chunks = new ArrayList<>();
        int maxLength = 600;

        for (int i = 0; i < text.length(); i += maxLength)
        {
            int end = Math.min(i + maxLength, text.length());
            chunks.add(text.substring(i, end).trim());
        }

        return chunks;
    }
}