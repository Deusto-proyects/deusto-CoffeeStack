package com.deusto.coffeestack.service;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Component
public class OllamaInvoker {

    private final OllamaChatModel chatModel;

    public OllamaInvoker(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Retryable(
            retryFor = { ResourceAccessException.class, HttpServerErrorException.class },
            maxAttemptsExpression = "${ai.chatbot.retry-max-attempts:3}",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 4000)
    )
    public ChatResponse invocar(Prompt prompt) {
        return chatModel.call(prompt);
    }
}
