package cdq.cdl.aiassistant.chat.infrastructure.inbound.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cdq.cdl.aiassistant.chat.application.QuestionAnsweringService;
import cdq.cdl.aiassistant.chat.domain.model.AssistantAnswer;
import cdq.cdl.aiassistant.chat.domain.model.UserQuestion;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatApi
{
    private final QuestionAnsweringService service;

    @PostMapping
    public ChatResponse askQuestion(@RequestBody ChatRequest request)
    {
        UserQuestion question = UserQuestion.of(request.question());
        AssistantAnswer answer = service.handle(question);
        return new ChatResponse(answer.value());
    }
}
