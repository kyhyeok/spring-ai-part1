package com.example.prompts.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service

/**
 * PromptTemplate — 변수 바인딩.
 * "문자열 연결(+)이 아니라 템플릿으로"
 *
 * 왜 템플릿인가: 구조와 데이터의 분리 — JdbcTemplate의 ?, Thymeleaf의 ${}와 같은 이유.
 * {변수} 문법은 기본 StTemplateRenderer 기준.
 */
@Service
class TravelService(builder: ChatClient.Builder) {

    private val chatClient: ChatClient = builder.build()

    /** ── 슬라이드 코드 그대로 ────────────────────────────────── */
    fun recommend(question: String): String? = null

    /**
     * 대비용 — 문자열 연결(+) 방식.
     * 강의에서 먼저 이 코드를 보여주고 가독성·이스케이프·인젝션 위험을 지적한 뒤
     * 위 템플릿 버전으로 넘어간다.
     */
    fun recommendByConcat(role: String, format: String, question: String): String? = null

    /** 역할·형식을 바꿔가며 템플릿의 재사용성을 확인 */
    fun recommendCustom(role: String, format: String, question: String): String? = null
}
