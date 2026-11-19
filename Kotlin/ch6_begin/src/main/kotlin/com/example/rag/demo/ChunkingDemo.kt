package com.example.rag.demo

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

/**
 * 데모 (2) 청킹 비교
 *
 * 같은 문서를 청크 크기 200 / 500 / 1000 으로 분할해
 * "정밀 매칭 vs 문맥 보존" 트레이드오프를 눈으로 확인한다.  (교안 11장 7절)
 */
@Component
class ChunkingDemo(
    @param:Value("classpath:docs/company-policy.txt") private val policyDoc: Resource,
) {

    fun run() {
        println("=== 데모 (2) 청킹 비교 ===\n")

        // chunking

        println(
            """
            [관찰 포인트]
              - 작은 청크 : 조각이 정밀하지만 조항 중간에서 끊길 수 있다 (문맥 단절)
              - 큰 청크   : 문맥은 온전하지만 무관한 내용이 함께 딸려온다 (노이즈)
              - 정답 크기는 없다. 문서 성격에 따라 다르며 Section 8에서 '평가'로 찾는다.
            """.textBlock()
        )
    }

    private fun preview(text: String): String {
        // trim 기준은 Java String.trim()과 같게 (코드값 ' ' 이하만 제거)
        val flat = text.replace(WHITESPACE, " ").trim { it <= ' ' }
        return if (flat.length <= 90) flat else flat.take(90) + " ..."
    }
}

private val WHITESPACE = Regex("\\s+")
