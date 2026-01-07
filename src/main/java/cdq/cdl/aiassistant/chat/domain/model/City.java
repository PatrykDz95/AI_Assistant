package cdq.cdl.aiassistant.chat.domain.model;

import org.apache.commons.lang3.StringUtils;

public record City(String name)
{
    public City
    {
        if (StringUtils.isEmpty(name))
        {
            throw new IllegalArgumentException("City name cannot be empty");
        }
    }

    public static City of(String name)
    {
        return new City(name.trim());
    }
}

