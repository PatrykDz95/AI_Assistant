package cdq.cdl.aiassistant.chat.infrastructure.outbound.langchain.tools;

import org.springframework.stereotype.Component;

import cdq.cdl.aiassistant.chat.domain.port.ProductKnowledgePort;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductKnowledgeTool
{
    private final ProductKnowledgePort knowledgePort;

    @Tool(value = "Search CDQ product knowledge base. Use this when user asks about: CDQ, CDQ Fraud Guard, AML Guard, AML, anti-money laundering, fraud detection, financial crime, compliance. Always use this tool for CDQ or AML related questions.",
            name = "searchCDQProducts")
    public String searchProductKnowledge(String query)
    {
        log.info("[TOOL CALL] ProductKnowledgeTool.searchProductKnowledge [{}]", query);

        String result = knowledgePort.search(query);

        log.info("[TOOL RESULT] ProductKnowledgeTool.searchProductKnowledge [{}])", result);
        return result;
    }
}

