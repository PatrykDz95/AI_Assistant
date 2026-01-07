package cdq.cdl.aiassistant.chat.infrastructure.outbound.langchain.tools;

import org.springframework.stereotype.Component;

import cdq.cdl.aiassistant.chat.domain.model.City;
import cdq.cdl.aiassistant.chat.domain.model.Temperature;
import cdq.cdl.aiassistant.chat.domain.port.WeatherPort;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherTool
{
    private final WeatherPort weatherPort;

    @Tool("Get the current temperature in Celsius for a given city")
    public double getCurrentTemperature(String cityName)
    {
        log.debug("Tool execution started: tool=getCurrentTemperature, query=[{}]", cityName);

        Temperature temperature = weatherPort.getCurrentTemperature(City.of(cityName));

        log.debug("Tool execution ended: tool=getCurrentTemperature, result=[{}°C]", temperature.celsius());
        return temperature.celsius();
    }
}

