package cdq.cdl.aiassistant.chat.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.*;

import cdq.cdl.aiassistant.chat.domain.model.AssistantAnswer;
import cdq.cdl.aiassistant.chat.domain.model.UserQuestion;
import cdq.cdl.aiassistant.chat.domain.port.AiReasoningPort;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatApplicationServiceTest
{
    @Mock
    private AiReasoningPort aiAgent;

    private ChatApplicationService service;

    @BeforeEach
    void setUp()
    {
        service = new ChatApplicationService(aiAgent);
    }

    @Test
    void shouldDelegateQuestionToAiAgent()
    {
        // Given
        UserQuestion question = new UserQuestion("What is the capital of Germany?");
        AssistantAnswer expectedAnswer = new AssistantAnswer("The capital of Germany is Berlin.");

        when(aiAgent.answer(any(UserQuestion.class))).thenReturn(expectedAnswer);

        // When
        AssistantAnswer actualAnswer = service.handle(question);

        // Then
        assertThat(actualAnswer).isEqualTo(expectedAnswer);
        verify(aiAgent, times(1)).answer(question);
    }

    @Test
    void shouldReturnErrorMessageWhenAiAgentFails()
    {
        // Given
        UserQuestion question = new UserQuestion("Test question");

        when(aiAgent.answer(any(UserQuestion.class)))
                .thenThrow(new RuntimeException("AI service unavailable"));

        // When
        AssistantAnswer answer = service.handle(question);

        // Then
        assertThat(answer.value())
                .contains("error")
                .contains("AI service unavailable");
    }

    @Test
    void shouldHandleNullPointerExceptions()
    {
        // Given
        UserQuestion question = new UserQuestion("Test question");

        when(aiAgent.answer(any(UserQuestion.class)))
                .thenThrow(new NullPointerException("Null value encountered"));

        // When
        AssistantAnswer answer = service.handle(question);

        // Then
        assertThat(answer.value())
                .contains("error")
                .contains("Null value encountered");
    }
}

