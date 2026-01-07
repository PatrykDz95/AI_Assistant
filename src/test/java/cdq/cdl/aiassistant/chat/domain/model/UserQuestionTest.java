package cdq.cdl.aiassistant.chat.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UserQuestionTest
{
    @Test
    void shouldCreateValidUserQuestion()
    {
        // Given
        String questionText = "What is the capital of Germany?";

        // When
        UserQuestion question = new UserQuestion(questionText);

        // Then
        assertThat(question.value()).isEqualTo(questionText);
    }

    @Test
    void shouldRejectEmptyQuestion()
    {
        // When/Then
        assertThatThrownBy(() -> new UserQuestion(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Question must not be empty");
    }

    @Test
    void shouldRejectNullQuestion()
    {
        // When/Then
        assertThatThrownBy(() -> new UserQuestion(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Question must not be empty");
    }
}

