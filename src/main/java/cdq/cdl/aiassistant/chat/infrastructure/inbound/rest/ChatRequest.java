package cdq.cdl.aiassistant.chat.infrastructure.inbound.rest;

import org.apache.commons.lang3.StringUtils;

public record ChatRequest(String question)
{
    public ChatRequest
    {
        if (StringUtils.isBlank(question))
        {
            throw new IllegalArgumentException("Question must not be empty");
        }
    }
}
