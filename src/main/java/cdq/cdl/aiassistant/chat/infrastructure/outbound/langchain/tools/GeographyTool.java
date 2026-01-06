package cdq.cdl.aiassistant.chat.infrastructure.outbound.langchain.tools;

import org.springframework.stereotype.Component;

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

    @Tool("Get the capital city of a given country")
    public String getCapitalCity(String countryName)
    {
        log.info("[TOOL CALL] GeographyTool.getCapitalCity [{}]", countryName);

        String result = countriesPort.capitalOf(countryName);

        log.info("[TOOL RESULT] GeographyTool.getCapitalCity [{}]", result);
        return result;
    }

    @Tool("Get detailed information about a city (works for capital cities)")
    public String getCityInformation(String cityName)
    {
        log.info("[TOOL CALL] GeographyTool.getCityInformation [{}]", cityName);

        String result = countriesPort.aboutCity(cityName);

        log.info("[TOOL RESULT] GeographyTool.getCityInformation [{}]", result);
        return result;
    }
}

