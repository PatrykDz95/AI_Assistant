package cdq.cdl.aiassistant.chat.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AssistantAnswerTest
{
    @Test
    void shouldCreateValidAssistantAnswer()
    {
        // Given
        String answerText = "The capital of Germany is Berlin.";

        // When
        AssistantAnswer answer = new AssistantAnswer(answerText);

        // Then
        assertThat(answer.value()).isEqualTo(answerText);
    }

    @Test
    void shouldRejectEmptyAnswer()
    {
        // When/Then
        assertThatThrownBy(() -> new AssistantAnswer(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Answer must not be empty");
    }

    @Test
    void shouldRejectNullAnswer()
    {
        // When/Then
        assertThatThrownBy(() -> new AssistantAnswer(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Answer must not be empty");
    }

    @Test
    void shouldRejectBlankAnswer()
    {
        // When/Then
        assertThatThrownBy(() -> new AssistantAnswer("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Answer must not be empty");
    }
}

