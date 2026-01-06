package cdq.cdl.aiassistant.chat.infrastructure.outbound.langchain.tools;

import org.springframework.stereotype.Component;

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
        log.info("[TOOL CALL] WeatherTool.getCurrentTemperature [{}]", cityName);

        double result = weatherPort.currentTemperatureCelsius(cityName);

        log.info("[TOOL RESULT] WeatherTool.getCurrentTemperature [{}°C])", result);
        return result;
    }
}

