package com.hyeok.springai.services

import com.hyeok.springai.model.Answer
import com.hyeok.springai.model.Question
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class OpenAIServiceImpl(
    private val chatModel: ChatModel,
) : OpenAIService {

    // Kotlin 문자열에서 '$'는 템플릿 문법이므로 '\$'로 이스케이프
    @Value("\${spring.ai.openai.chat.model:기본값 사용}")
    private lateinit var modelName: String

    override fun getAnswer(question: Question): Answer {
        println("현재 사용하는 ChatModel 구현체: ${chatModel.javaClass.name}")
        println("현재 설정된 OpenAI 모델: $modelName")

        val promptTemplate = PromptTemplate(question.question)
        val prompt = promptTemplate.create()

        val response = chatModel.call(prompt)

        // getResult()는 @Nullable — Java는 여기서 NPE(500)가 나고, Kotlin도 같은 500이지만 원인을 메시지로 남긴다
        val result = checkNotNull(response.result) { "ChatResponse에 결과(Generation)가 없습니다" }

        // getText()도 @Nullable — Java와 동일하게 null이면 null을 그대로 Answer에 담는다
        return Answer(result.output.text)
    }
}
