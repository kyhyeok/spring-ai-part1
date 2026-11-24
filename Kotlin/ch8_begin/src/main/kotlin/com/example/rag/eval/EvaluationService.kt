package com.example.rag.eval

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.evaluation.RelevancyEvaluator
import org.springframework.ai.evaluation.EvaluationRequest
import org.springframework.ai.evaluation.EvaluationResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

/**
 * 평가 세트 기반 품질 측정 (교안 8장 6~9절)
 *
 * 측정 지표
 *   - Relevancy 통과율   : LLM-as-a-Judge 로 질문-답변 관련성 판정
 *   - "없다" 정답 처리율  : trap 문항에서 환각을 억제했는가
 *   - 평균 근거 토큰      : 컨텍스트 다이어트 효과 (비용 지표)
 */
@Service
class EvaluationService(
    private val ragService: RagService,
    builder: ChatClient.Builder,
    objectMapper: ObjectMapper,
    @Value("classpath:eval/evaluation-set.json") evalSetResource: Resource,
) {

    private val relevancyEvaluator = RelevancyEvaluator(builder)
    private val evaluationSet: EvaluationSet =
        evalSetResource.inputStream.use { objectMapper.readValue<EvaluationSet>(it) }

    fun run(): Report {
        val items = mutableListOf<ItemResult>()

        for (q in evaluationSet.questions) {
            val r = ragService.ask(q.question)
            // Java는 답변이 null이면 contains()에서 NPE — 같은 자리에서 원인 메시지와 함께 실패
            val answer = checkNotNull(r.answer) { "답변이 없습니다: ${q.id}" }
            val saidNotFound = NOT_FOUND_MARKER in answer

            val pass: Boolean
            val note: String

            when {
                !q.answerable -> {
                    // 함정 문항: "확인할 수 없습니다"라고 답해야 통과

                }
                saidNotFound -> {
                    // 답할 수 있어야 하는데 못 찾음 -> 검색 실패

                }
                else -> {
                    // LLM-as-a-Judge 로 관련성 판정
                    val req: EvaluationRequest? = null // 평가에 필요한 입력값을 담는 객체
                    val res: EvaluationResponse? = null // 평가 결과를 담는 객체

                }
            }

//            items.add(ItemResult(q.id, q.type, q.question, pass, note,
//                    r.contextTokensApprox, preview(answer)))
//            log.info("[{}] {} - {}", q.id, if (pass) "PASS" else "FAIL", note)
        }

        return summarize(items)
    }

    private fun summarize(items: List<ItemResult>): Report {
        val total = 0L
        val passed = 0L

        val byType = mutableMapOf<String, String>()
        for (type in listOf("general", "boundary", "trap")) {

        }

        val avgTokens = 0.0
        val failed: List<String>? = null

        return Report(
            // Java formatted()와 같은 java.util.Formatter (기본 로캘)
            "%d/%d (%.0f%%)".format(passed, total, pct(passed, total)),
            byType,
            Math.round(avgTokens),  // roundToLong()과 달리 NaN이면 0 (Java와 같음)
            failed,
            items,
            "회귀 확인: 이전 실행의 failedIds 와 비교하라. 전체 통과율이 올라도 개별 문항이 깨질 수 있다.",
        )
    }

    data class ItemResult(
        val id: String,
        val type: String,
        val question: String,
        val pass: Boolean,
        val note: String,
        val contextTokensApprox: Int,
        val answerPreview: String,
    )

    data class Report(
        val overallPassRate: String,
        val passRateByType: Map<String, String>,
        val avgContextTokensApprox: Long,
        val failedIds: List<String>?,
        val items: List<ItemResult>,
        val hint: String,
    )

    companion object {
        private val log = LoggerFactory.getLogger(EvaluationService::class.java)
        private const val NOT_FOUND_MARKER = "확인할 수 없습니다"
    }
}

private val WHITESPACE = Regex("\\s+")

private fun pct(n: Long, d: Long): Double = if (d == 0L) 0.0 else n * 100.0 / d

private fun preview(s: String): String {
    // trim 기준은 Java String.trim()과 같게 (코드값 ' ' 이하만 제거)
    val flat = s.replace(WHITESPACE, " ").trim { it <= ' ' }
    return if (flat.length <= 120) flat else flat.take(120) + " ..."
}
