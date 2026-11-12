package com.hyeok.springai.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.memory.MessageWindowChatMemory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatConfig {

    @Bean
    fun chatMemory(): ChatMemory =
        MessageWindowChatMemory.builder()
            .maxMessages(10)    // 최근 10개 메시지만 유지
            .build()

    // ChatClient.Builder는 prototype 빈이라 주입받을 때마다 새 builder가 온다.
    // 그래서 여기서 붙인 기억 advisor는 MovieServiceImpl이 받는 builder에는 영향을 주지 않는다.
    @Bean
    fun memoryChatClient(builder: ChatClient.Builder, chatMemory: ChatMemory): ChatClient =
        builder
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .build()
}
