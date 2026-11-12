package com.hyeok.springai.services

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class MemoryChatService(
    @Qualifier("memoryChatClient") private val chatClient: ChatClient,
) {

    fun chat(conversationId: String, message: String): String? =
        promptWithMemory(conversationId, message)
            .call()
            .content()

    /* 대화기억 + 스트리밍 처리 */
    fun chatStream(conversationId: String, message: String): Flux<String> =
        promptWithMemory(conversationId, message)
            .stream()
            .content()

    // 일반 호출과 스트리밍이 같은 대화 ID로 기억을 이어 가도록 요청 구성을 한 곳에 모은다
    private fun promptWithMemory(
        conversationId: String,
        message: String,
    ): ChatClient.ChatClientRequestSpec =
        chatClient.prompt()
            .user(message)
            .advisors { it.param(ChatMemory.CONVERSATION_ID, conversationId) }
}
