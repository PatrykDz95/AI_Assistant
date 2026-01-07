package cdq.cdl.aiassistant.chat.application;

import org.springframework.stereotype.Service;

import cdq.cdl.aiassistant.chat.domain.model.AssistantAnswer;
import cdq.cdl.aiassistant.chat.domain.model.UserQuestion;
import cdq.cdl.aiassistant.chat.domain.port.AiReasoningPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionAnsweringService
{
    private final AiReasoningPort aiAgent;

    public AssistantAnswer handle(UserQuestion question)
    {
        try
        {
            return aiAgent.answer(question);
        }
        catch (Exception e)
        {
            log.error("Error handling question: [{}]", question.value(), e);
            return new AssistantAnswer("I apologize, but I encountered an error: " + e.getMessage());
        }
    }
}

