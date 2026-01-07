package cdq.cdl.aiassistant.chat.domain.model;

import org.apache.commons.lang3.StringUtils;

public record CityInformation(
        City city,
        Country country,
        String region,
        long population,
        String currency
)
{
    public CityInformation
    {
        if (city == null)
        {
            throw new IllegalArgumentException("City cannot be null");
        }
        if (country == null)
        {
            throw new IllegalArgumentException("Country cannot be null");
        }
        if (StringUtils.isEmpty(region))
        {
            throw new IllegalArgumentException("Region cannot be empty");
        }
        if (population < 0)
        {
            throw new IllegalArgumentException("Population cannot be negative");
        }
        if (StringUtils.isEmpty(currency))
        {
            throw new IllegalArgumentException("Currency cannot be empty");
        }
    }

    public String toDescription()
    {
        return String.format(
                "%s is the capital of %s. " +
                        "Country region: %s. " +
                        "Population: %,d. " +
                        "Currency: %s.",
                city.name(),
                country.name(),
                region,
                population,
                currency
        );
    }
}

