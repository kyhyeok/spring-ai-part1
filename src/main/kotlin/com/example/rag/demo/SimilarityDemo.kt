package com.example.rag.demo

import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.stereotype.Component
import kotlin.math.max
import kotlin.math.sqrt

/**
 * 데모 (1) 임베딩 유사도
 *
 * 교안 10장의 "고양이 / 강아지 / 동물 / 자동차" 예시를 실제 수치로 확인한다.
 * 슬라이드의 예시값(0.86 / 0.79 / 0.21)과 비교하며
 * "절대값이 아니라 순위와 격차가 의미 있다"는 감각을 잡는 것이 목적.
 */
@Component
class SimilarityDemo(
    private val embeddingModel: EmbeddingModel,
) {

    fun run() {
        println("=== 데모 (1) 임베딩 유사도 ===\n")

        val query = "고양이"
        val candidates = mapOf(
            "강아지" to "강아지",
            "동물" to "동물",
            "반려묘를 키우는 법" to "반려묘를 키우는 법",
            "자동차" to "자동차",
            "주식 투자 전략" to "주식 투자 전략",
        )

        // embedding

        println(
            """

            [관찰 포인트]
              1) 단어가 완전히 달라도 의미가 가까우면 유사도가 높다  (고양이 vs 강아지)
              2) 무관한 주제는 확연히 낮다                          (고양이 vs 주식)
              3) 절대값은 모델마다 다르다 - 중요한 것은 '순위와 격차'
                 -> Section 8에서 similarityThreshold 를 자기 데이터로 캘리브레이션해야 하는 이유
            """.textBlock()
        )
    }

    companion object {
        /** 코사인 유사도 = (A dot B) / (|A| * |B|) */
        internal fun cosineSimilarity(a: FloatArray, b: FloatArray): Double {
            var dot = 0.0
            var na = 0.0
            var nb = 0.0
            for (i in a.indices) {
                // Java와 같이 float끼리 곱한 뒤 double에 더한다
                dot += a[i] * b[i]
                na += a[i] * a[i]
                nb += b[i] * b[i]
            }
            return dot / (sqrt(na) * sqrt(nb))
        }

        private fun bar(sim: Double): String {
            // roundToInt()는 NaN(영벡터 비교)에서 예외를 던지므로 Java와 같은 Math.round(NaN → 0)를 쓴다
            val n = Math.round(max(0.0, sim) * 40).toInt()
            return "#".repeat(n)
        }
    }
}
