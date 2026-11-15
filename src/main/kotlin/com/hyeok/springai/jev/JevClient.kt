package com.hyeok.springai.jev

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.requiredBody

@Component
class JevClient(
    private val props: JevProperties,
) {
    private val restClient: RestClient = RestClient.builder()
        // Java처럼 base-url 설정이 없어도 빈은 만들어지도록, 값이 있을 때만 지정한다
        .also { builder -> props.baseUrl?.let { builder.baseUrl(it) } }
        .defaultHeader("Authorization", "Bearer ${props.apiKey}")
        .build()

    fun enabled(): Boolean = props.enabled

    /** state 하나에 여러 질문을 보내고, 질문 key별 결정을 돌려받습니다. */
    fun decide(state: Any, questions: Map<String, Any>): Map<String, Decision> =
        restClient.post()
            .uri("/v1/systemone")
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("model" to props.model, "state" to state, "questions" to questions))
            .retrieve()
            .requiredBody<JevResponse>() // 응답 본문이 비면 예외 (Java는 NPE)
            .answers

    // model은 코드에서 읽지 않고 응답에 항상 온다는 근거도 없어 nullable (Java처럼 빠져도 실패하지 않게)
    data class JevResponse(
        val model: String?,
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
