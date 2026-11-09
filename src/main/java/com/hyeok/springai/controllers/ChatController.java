package com.hyeok.springai.controllers;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
public class ChatController {
    private final ChatClient chatClient;

	public ChatController(ChatClient.Builder builder) {
		this.chatClient = builder.build();
	}

	@GetMapping("/ask")
	public String ask(@RequestParam String question) {
		return chatClient.prompt()
			.user(question)
			.call()
			.content();
	}

	@GetMapping("/stream")
	public String stream(@RequestParam String question) {
		return null;
	}
}

