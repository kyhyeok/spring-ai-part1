package com.hyeok.springai

import org.springframework.ai.chat.client.ChatClient
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class AiRunner(builder: ChatClient.Builder) : CommandLineRunner {
    private val chatClient: ChatClient = builder.build()

    // promp -> user/system -> call/string -> content/entity
    override fun run(vararg args: String) {
        val answer = chatClient.prompt()
            .user("Spring AI를 한 문장으로 정의해줘")
            .call()
            .content()
        println("answer = $answer")
    }
}
