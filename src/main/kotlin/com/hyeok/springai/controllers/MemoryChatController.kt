package com.hyeok.springai.controllers

import com.hyeok.springai.services.MemoryChatService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/chat")
class MemoryChatController(
    private val memoryChatService: MemoryChatService,
) {

    /*
     * POST /api/chat/kenneth       body: "제 이름 Kenneth입니다"
     * POST /api/chat/kenneth       body: "제 이름이 뭐라고 했죠?" -> 기억?
     */
    // content()가 @Nullable이라 String?로 반환한다. null이면 Java와 같이 빈 본문 200
    @PostMapping("/{conversationId}")
    fun chat(
        @PathVariable conversationId: String,
        @RequestBody message: String,
    ): String? = memoryChatService.chat(conversationId, message)

    @GetMapping("/{conversationId}/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(
        @PathVariable conversationId: String,
        @RequestParam message: String,
    ): Flux<String> = memoryChatService.chatStream(conversationId, message)
}
