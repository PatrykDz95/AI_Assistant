package cdq.cdl.aiassistant.chat.infrastructure.outbound.langchain.tools;

import org.springframework.stereotype.Component;

import cdq.cdl.aiassistant.chat.domain.model.City;
import cdq.cdl.aiassistant.chat.domain.model.CityInformation;
import cdq.cdl.aiassistant.chat.domain.model.Country;
import cdq.cdl.aiassistant.chat.domain.port.CountriesPort;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeographyTool
{
    private final CountriesPort countriesPort;

    @Tool("Get the capital city of a given countryName")
    public String getCapitalCity(String countryName)
    {
        log.debug("Tool execution started: tool=getCapitalCity, query=[{}]", countryName);

        City capital = countriesPort.getCapitalOf(Country.of(countryName));

        log.debug("Tool execution ended: tool=getCapitalCity, result=[{}]", capital.name());
        return capital.name();
    }

    @Tool("Get detailed information about the given cityName")
    public String getCityInformation(String cityName)
    {
        log.debug("Tool execution started: tool=getCityInformation, query=[{}]", cityName);

        CityInformation info = countriesPort.getCityInformation(City.of(cityName));
        String description = info.toDescription();

        log.debug("Tool execution ended: tool=getCityInformation, result=[{}]", description);
        return description;
    }
}

