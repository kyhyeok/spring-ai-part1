package com.example.prompts.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

/**
 * Prompt 외부 파일 관리 — "Prompt는 소스코드"의 물리적 실현.
 *
 * 분리하면 생기는 것
 *  ① 프롬프트 수정이 코드 리뷰의 대상이 됨
 *  ② Kotlin 코드와 독립적으로 diff·이력 추적
 *  ③ 기획자·도메인 전문가와의 협업 지점 확보
 */
@Service
class ChatService(builder: ChatClient.Builder) {

    // begin 코드의 빈자리: Java처럼 아직 주입 설정(@Value)이 없어 비어 있다.
    // 그래서 currentSystemPrompt()는 Java와 같이 이 값을 쓰는 지점에서 예외가 난다.
    private lateinit var systemResource: Resource

    private val chatClient: ChatClient = builder.build()

    /**
     * Resource를 system()에 직접 전달.
     * 이 경우 파일 안의 {service} 자리는 치환되지 않고 그대로 전달된다는 점을 시연에서 짚어준다.
     */
    fun ask(question: String): String? = null

    /**
     * 실전형 — 파일의 {service} 변수를 바인딩한 뒤 전달.
     * PromptTemplate과 외부 파일 관리를 결합한 형태로, 실무에서 쓰는 조합이다.
     */
    fun ask(service: String, question: String): String? = null

    /** 현재 로딩된 프롬프트 파일 원문 확인 — "프롬프트도 리뷰 대상"임을 보여주는 용도 */
    fun currentSystemPrompt(): String = PromptTemplate(systemResource).template
}
