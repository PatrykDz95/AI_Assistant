package cdq.cdl.aiassistant.chat.infrastructure.outbound.weather;

import java.util.Map;

import org.springframework.stereotype.Component;

import cdq.cdl.aiassistant.chat.domain.model.City;
import cdq.cdl.aiassistant.chat.domain.model.Temperature;
import cdq.cdl.aiassistant.chat.domain.port.WeatherPort;
import cdq.cdl.aiassistant.chat.infrastructure.mcp.McpHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class McpWeatherAdapter implements WeatherPort
{
    private final McpHttpClient mcp;

    @Override
    public Temperature getCurrentTemperature(City city)
    {
        log.info("Fetching current temperature for city: [{}]", city.name());

        JsonNode result = mcp.callTool(
                "get_current_weather",
                Map.of(
                        "city", city.name(),
                        "units", "metric"
                )
        );

        JsonNode temp = result.get("temperature");

        if (temp == null)
        {
            throw new IllegalStateException("Temperature missing in MCP response: " + result);
        }

        return new Temperature(temp.asDouble());
    }
}
