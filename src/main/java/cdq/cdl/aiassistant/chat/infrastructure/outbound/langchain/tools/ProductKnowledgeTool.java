package cdq.cdl.aiassistant.chat.infrastructure.outbound.langchain.tools;

import org.springframework.stereotype.Component;

import cdq.cdl.aiassistant.chat.domain.port.ProductKnowledgePort;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductKnowledgeTool
{
    private final ProductKnowledgePort knowledgePort;

    @Tool(value = "Search the CDQ product knowledge base for information about CDQ products and services. "
            + "ALWAYS use this tool when the question mentions: "
            + "'CDQ Fraud Guard', 'Fraud Guard', 'AML Guard', 'CDQ', 'AML', 'anti-money laundering', "
            + "'fraud detection', 'financial crime', 'compliance', 'risk assessment', 'transaction monitoring'. "
            + "Examples of questions that require this tool: "
            + "'What is CDQ Fraud Guard?', 'Tell me about AML Guard', 'What does CDQ do?', "
            + "'How does fraud detection work?', 'What is AML?'",
          name = "searchCDQProductKnowledge")
    public String searchProductKnowledge(
            @P("The search query to find relevant CDQ product information") String query)
    {
        log.debug("Tool execution started: tool=searchProductKnowledge, query=[{}]", query);

        String result = knowledgePort.search(query);

        log.debug("Tool execution ended: tool=searchProductKnowledge, result=[{}]", result);
        return result;
    }
}

