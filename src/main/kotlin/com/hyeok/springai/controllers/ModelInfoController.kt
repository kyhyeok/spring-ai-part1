package com.hyeok.springai.controllers

import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ai")
class ModelInfoController(
    private val chatModel: ChatModel,
) {

    // Kotlin 문자열에서 '$'는 템플릿 문법이므로 '\$'로 이스케이프
    @Value("\${spring.ai.openai.chat.model:기본값 사용}")
    private lateinit var modelName: String

    @GetMapping("/model")
    fun getModelInfo(): Map<String, String> {
        if (chatModel is OpenAiChatModel) {
            modelName = chatModel.options.model
            println("현재 실제 사용 모델: $modelName")
        }

        return mapOf(
            "chatModelClass" to chatModel.javaClass.name,
            "modelName" to modelName,
        )
    }
}
