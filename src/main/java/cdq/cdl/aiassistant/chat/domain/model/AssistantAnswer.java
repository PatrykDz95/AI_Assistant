package cdq.cdl.aiassistant.chat.domain.model;

import org.apache.commons.lang3.StringUtils;

public record AssistantAnswer(String value)
{
    public AssistantAnswer
    {
        if (StringUtils.isBlank(value))
        {
            throw new IllegalArgumentException("Answer must not be empty");
        }
    }
}
