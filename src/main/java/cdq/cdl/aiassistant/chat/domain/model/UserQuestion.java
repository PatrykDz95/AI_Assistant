package cdq.cdl.aiassistant.chat.domain.model;

import org.apache.commons.lang3.StringUtils;

public record UserQuestion(String value)
{
    public UserQuestion
    {
        if (StringUtils.isBlank(value))
        {
            throw new IllegalArgumentException("Question must not be empty");
        }
    }
}