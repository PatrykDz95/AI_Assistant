package cdq.cdl.aiassistant.chat.domain.model;

import org.apache.commons.lang3.StringUtils;

public record Country(String name)
{
    public Country
    {
        if (StringUtils.isEmpty(name))
        {
            throw new IllegalArgumentException("Country name cannot be empty");
        }
    }

    public static Country of(String name)
    {
        return new Country(name.trim());
    }
}

