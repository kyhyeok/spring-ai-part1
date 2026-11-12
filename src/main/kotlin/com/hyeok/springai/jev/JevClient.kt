package com.hyeok.springai.jev

import org.springframework.web.client.RestClient

//@Component
class JevClient(
    private val props: JevProperties,
) {
    // begin 코드의 빈자리: 이후 강의에서 RestClient를 만든다. 지금은 Java와 같이 null
    private val restClient: RestClient? = null

    fun enabled(): Boolean = props.enabled

    /** state 하나에 여러 질문을 보내고, 질문 key별 결정을 돌려받습니다. */
    // begin 코드의 빈자리: Java와 같이 지금은 null을 돌려준다
    fun decide(state: Any, questions: Map<String, Any>): Map<String, Decision>? = null

    data class JevResponse(
        val model: String,
        val answers: Map<String, Decision>,
    )

    /** 질문 타입에 따라 채워지는 필드가 다릅니다 (choice / score / noul). */
    // Jev API 문서 기준으로 type만 항상 오고 나머지는 질문 타입에 따라 비어 있을 수 있어 nullable
    data class Decision(
        val type: String,
        val choice: String?,
        val score: Double?,
        val recommend: String?,
        val noul: Double?,
        val confidence: Double?,
        val probabilities: Map<String, Double>?,
    )
}
