package cdq.cdl.aiassistant.chat.infrastructure.inbound.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cdq.cdl.aiassistant.chat.application.ChatApplicationService;
import cdq.cdl.aiassistant.chat.domain.model.AssistantAnswer;
import cdq.cdl.aiassistant.chat.domain.model.UserQuestion;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController
{
    private final ChatApplicationService service;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request)
    {
        AssistantAnswer answer = service.handle(new UserQuestion(request.question()));
        return new ChatResponse(answer.value());
    }
}
