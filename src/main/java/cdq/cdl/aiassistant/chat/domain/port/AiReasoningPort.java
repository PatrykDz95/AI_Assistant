package cdq.cdl.aiassistant.chat.domain.port;

import cdq.cdl.aiassistant.chat.domain.model.AssistantAnswer;
import cdq.cdl.aiassistant.chat.domain.model.UserQuestion;

public interface AiReasoningPort
{
    AssistantAnswer answer(UserQuestion question);
}
