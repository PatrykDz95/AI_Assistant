package cdq.cdl.aiassistant.chat.infrastructure.outbound.langchain;

import org.springframework.stereotype.Component;

import cdq.cdl.aiassistant.chat.domain.model.AssistantAnswer;
import cdq.cdl.aiassistant.chat.domain.model.UserQuestion;
import cdq.cdl.aiassistant.chat.domain.port.AiReasoningPort;
import cdq.cdl.aiassistant.shared.config.LangChain4jConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class LangChainAiAdapter implements AiReasoningPort
{
    private final LangChain4jConfig.AssistantAI assistantAI;

    @Override
    public AssistantAnswer answer(UserQuestion question)
    {
        log.info("Processing question: [{}]", question.value());
        try
        {
            String response = assistantAI.chat(question.value());

            return new AssistantAnswer(response);
        }
        catch (Exception e)
        {
            log.error("Processing error [{}]", e.getMessage(), e);
            throw e;
        }
    }
}

