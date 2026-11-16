package com.example.prompts.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.PromptTemplate
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

    /**
     * ── 슬라이드 코드 그대로 ──────────────────────────────────
     * 슬라이드의 흐름(PromptTemplate 생성 → create(Map) → prompt(Prompt))은 recommendCustom과 함께 쓰는
     * askWithTemplate에 있고, 템플릿 문구는 TEMPLATE에 있다. 여기서는 역할·형식 값만 정한다.
     */
    fun recommend(question: String): String? =
        askWithTemplate(role = "여행 전문가", format = "번호 목록 3개", question = question)

    /**
     * 대비용 — 문자열 연결(+) 방식.
     * 강의에서 먼저 이 코드를 보여주고 가독성·이스케이프·인젝션 위험을 지적한 뒤
     * 위 템플릿 버전으로 넘어간다.
     */
    fun recommendByConcat(role: String, format: String, question: String): String? {
        // 대비용 예제라 Kotlin 문자열 템플릿 대신 Java와 같은 + 연결을 그대로 보여 준다 (문구도 Java와 글자 단위로 같음)
        val prompt = "당신은 " + role + "입니다.\n" +
            "아래 질문에 " + format + " 형식으로 답하세요. \n\n" +
            "질문: " + question

        return chatClient.prompt().user(prompt).call().content()
    }

    /** 역할·형식을 바꿔가며 템플릿의 재사용성을 확인 */
    fun recommendCustom(role: String, format: String, question: String): String? =
        askWithTemplate(role, format, question)

    // recommend와 recommendCustom은 같은 템플릿에 값만 달리 넣으므로 템플릿과 호출을 한 곳에 둔다
    private fun askWithTemplate(role: String, format: String, question: String): String? {
        val prompt = PromptTemplate(TEMPLATE).create(
            mapOf("role" to role, "format" to format, "question" to question),
        )

        return chatClient.prompt(prompt).call().content()
    }

    companion object {
        // Java 텍스트 블록과 글자 단위로 같은 문자열 (끝 줄바꿈 포함)
        private val TEMPLATE = """
            당신은 {role} 입니다.
            아래 질문에 {format} 형식으로 답하세요.

            질문: {question}
            """.textBlock()
    }
}
