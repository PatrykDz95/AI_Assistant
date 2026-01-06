package cdq.cdl.aiassistant.chat.infrastructure.mcp;

import java.util.Map;
import java.util.UUID;

import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

public class McpHttpClient
{
    private final WebClient webClient;

    public McpHttpClient(WebClient webClient)
    {
        this.webClient = webClient;
    }

    public JsonNode callTool(String toolName, Map<String, Object> arguments)
    {
        try
        {
            var request = Map.of(
                    "jsonrpc", "2.0",
                    "id", UUID.randomUUID().toString(),
                    "method", "tools/call",
                    "params", Map.of(
                            "name", toolName,
                            "arguments", arguments
                    )
            );

            JsonNode response = webClient.post()
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || response.has("error"))
            {
                throw new IllegalStateException("MCP error: " + response);
            }

            return response.get("result");
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Failed to call MCP tool " + toolName, e);
        }
    }
}
