package com.hyeok.springai.controllers

import org.springframework.ai.chat.client.ChatClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/ai")
class ChatController(builder: ChatClient.Builder) {
    private val chatClient: ChatClient = builder.build()

    // content()는 응답이 비어 있으면 null일 수 있으므로 String? 로 반환
    @GetMapping("/ask")
    fun ask(@RequestParam question: String): String? =
        chatClient.prompt()
            .user(question)
            .call()
            .content()

    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(@RequestParam question: String): Flux<String> =
        chatClient.prompt()
            .user(question)
            .stream()
            .content()
}
