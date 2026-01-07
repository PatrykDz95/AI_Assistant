package cdq.cdl.aiassistant.chat.domain.model;

import org.apache.commons.lang3.StringUtils;

public record UserQuestion(String value)
{
    public UserQuestion
    {
        if (StringUtils.isEmpty(value))
        {
            throw new IllegalArgumentException("Question must not be empty");
        }
    }

    public static UserQuestion of(String question)
    {
        String trimmed = question == null ? null : question.trim();
        return new UserQuestion(trimmed);
    }
}