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
            @Value("${langchain4j.ollama.chat-model.timeout:120s}") Duration timeout)
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
                1. getCapitalCity(countryName) - Use when asked about capital cities
                Examples: "capital of Germany", "what is the capital"
                
                2. getCityInformation(cityName) - Use for information about a specific city
                Examples: "tell me about Berlin", "information about Munich", "what do you know about Berlin"
                
                3. getCurrentTemperature(cityName) - Use for weather/temperature questions
                Examples: "temperature in Munich", "how warm is Berlin", "weather"
                
                4. searchCDQProductKnowledge(query) - Use for CDQ product and AML questions
                ALWAYS use this tool when question contains ANY of these keywords:
                   - "CDQ" or "CDQ Fraud Guard" or "Fraud Guard"
                   - "AML" or "AML Guard" or "anti-money laundering"
                   - "fraud detection" or "financial crime"
                   - "compliance" or "risk assessment"
                   - "trust score" or "risk score"
                   - "transaction monitoring"
                Examples: "What is CDQ Fraud Guard?", "Tell me about AML", "What is the AML Guard?"
                
                CRITICAL RULES:
                - If question contains "CDQ Fraud Guard" -> MUST use searchCDQProductKnowledge
                - If question contains "AML" -> MUST use searchCDQProductKnowledge
                - If question mentions "trust score" or "risk score" -> MUST use searchCDQProductKnowledge
                - If question asks about a city name -> use getCityInformation
                - For "temperature of capital of X" -> call getCapitalCity FIRST, then getCurrentTemperature
                - NEVER say "I don't know" if a tool can answer the question
                - ALWAYS prefer using a tool over giving a generic answer
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



