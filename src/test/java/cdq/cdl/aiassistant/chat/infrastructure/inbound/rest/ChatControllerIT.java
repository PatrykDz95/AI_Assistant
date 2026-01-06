package cdq.cdl.aiassistant.chat.infrastructure.inbound.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import cdq.cdl.aiassistant.chat.application.ChatApplicationService;
import cdq.cdl.aiassistant.chat.domain.model.AssistantAnswer;
import cdq.cdl.aiassistant.chat.domain.model.UserQuestion;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerIT
{
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatApplicationService chatService;

    @Test
    void shouldReturnAnswerWhenValidQuestionProvided() throws Exception
    {
        // Given
        String requestBody = """
                {
                    "question": "What is the capital of Germany?"
                }
                """;

        AssistantAnswer expectedAnswer = new AssistantAnswer("The capital of Germany is Berlin.");
        when(chatService.handle(any(UserQuestion.class))).thenReturn(expectedAnswer);

        // When/Then
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.answer").value("The capital of Germany is Berlin."));
    }

    @Test
    void shouldReturn400WhenQuestionIsEmpty() throws Exception
    {
        // Given
        String requestBody = """
                {
                    "question": ""
                }
                """;

        // When/Then
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenQuestionIsMissing() throws Exception
    {
        // Given
        String requestBody = "{}";

        // When/Then
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenRequestBodyIsInvalid() throws Exception
    {
        // Given
        String requestBody = "invalid json";

        // When/Then
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleServiceExceptionsGracefully() throws Exception
    {
        // Given
        String requestBody = """
                {
                    "question": "What is the weather?"
                }
                """;

        when(chatService.handle(any(UserQuestion.class)))
                .thenReturn(new AssistantAnswer("I apologize, but I encountered an error: Service unavailable"));

        // When/Then
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").exists());
    }
}

