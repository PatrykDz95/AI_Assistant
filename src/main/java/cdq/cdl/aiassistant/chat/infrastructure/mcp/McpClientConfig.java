package cdq.cdl.aiassistant.chat.infrastructure.mcp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
class McpClientConfig
{

    @Bean
    McpHttpClient mcpHttpClient(WebClient.Builder builder)
    {
        return new McpHttpClient(
                builder
                        .baseUrl("http://localhost:3333/mcp")
                        .build()
        );
    }
}
