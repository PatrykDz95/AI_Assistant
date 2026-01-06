package cdq.cdl.aiassistant.shared.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cdq.cdl.aiassistant.chat.infrastructure.outbound.langchain.tools.GeographyTool;
import cdq.cdl.aiassistant.chat.infrastructure.outbound.langchain.tools.ProductKnowledgeTool;
import cdq.cdl.aiassistant.chat.infrastructure.outbound.langchain.tools.WeatherTool;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;

@Configuration
public class LangChain4jConfig
{
    @Bean
    public ChatLanguageModel chatLanguageModel(
            @Value("${langchain4j.ollama.chat-model.base-url:http://localhost:11434}") String baseUrl,
            @Value("${langchain4j.ollama.chat-model.model-name:qwen2.5:1.5b}") String modelName,
            @Value("${langchain4j.ollama.chat-model.temperature:0.7}") Double temperature,
            @Value("${langchain4j.ollama.chat-model.timeout:60s}") Duration timeout)
    {
        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .timeout(timeout)
                .build();
    }

    public interface AssistantAI
    {
        @SystemMessage("""
                You are a helpful AI assistant with access to tools. You MUST use these tools to answer questions accurately.
                
                TOOLS AVAILABLE:
                
                1. getCapitalCity(country) - Use when asked about capitals
                   Examples: "capital of Germany", "what is the capital"
                
                2. getCityInformation(city) - Use for city facts
                   Examples: "tell me about Berlin", "information about Munich"
                
                3. getCurrentTemperature(city) - Use for weather/temperature
                   Examples: "temperature in Munich", "how warm is Berlin", "weather"
                
                4. searchProductKnowledge(query) - ALWAYS USE when asked about:
                   - CDQ, CDQ Fraud Guard, AML Guard
                   - AML, anti-money laundering
                   - fraud detection, financial crime, compliance
                   Examples: "What is AML Guard?", "Tell me about CDQ Fraud Guard", "What is CDQ?"
                
                RULES:
                - ALWAYS use tools when the question matches the examples above
                - For "temperature of capital of X", call getCapitalCity FIRST, then getCurrentTemperature with the result
                - If a question mentions CDQ, AML, or fraud, you MUST call searchProductKnowledge
                - Do not say "I don't know" if a tool can answer the question
                """)
        String chat(String userMessage);
    }

    @Bean
    public AssistantAI assistantAI(
            ChatLanguageModel chatModel,
            GeographyTool geographyTool,
            WeatherTool weatherTool,
            ProductKnowledgeTool productKnowledgeTool)
    {
        return AiServices.builder(AssistantAI.class)
                .chatLanguageModel(chatModel)
                .tools(geographyTool, weatherTool, productKnowledgeTool)
                .build();
    }
}



