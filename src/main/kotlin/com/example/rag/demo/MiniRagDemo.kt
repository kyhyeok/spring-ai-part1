package com.example.rag.demo

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

/**
 * 데모 (3) 미니 RAG
 *
 * DB 없이 SimpleVectorStore(인메모리)만으로 RAG 전체 흐름을 한 번에 관찰한다.
 *   적재: 로딩 -> 청킹 -> 임베딩 -> 저장
 *   질의: 질문 임베딩 -> 유사도 검색 -> 근거 주입 -> 생성
 *
 * Section 7에서는 이 구조를 pgvector 기반으로 확장한다.
 */
@Component
class MiniRagDemo(
    builder: ChatClient.Builder,
    private val embeddingModel: EmbeddingModel,
    @param:Value("classpath:docs/company-policy.txt") private val policyDoc: Resource,
) {

    private val chatClient: ChatClient = builder.build()

    fun run() {
        println("=== 데모 (3) 미니 RAG (SimpleVectorStore) ===\n")

        // ---------- 1. 적재 ----------
        println("[1/3] 적재 중...")
        /* 구현 */

        val question = "연차 휴가는 며칠이고 언제까지 사용해야 하나요?"

        // ---------- 2. 검색 ----------
        println("[2/3] 유사도 검색 (LLM 호출 전 - 근거 확인)")
        println("      질문: $question")
        /* 구현 */
        println()

        // ---------- 3. 생성 ----------
        println("[3/3] 근거를 주입해 답변 생성")
        /* 구현 */

        val withRag = ""

        val withoutRag = ""

        println("\n--- RAG 적용 답변 ---")
        println(withRag)
        println("\n--- RAG 미적용 답변 (비교용) ---")
        println(withoutRag)

        println(
            """

            [관찰 포인트]
              - RAG 적용: 우리 문서의 구체적 조항을 인용한다
              - RAG 미적용: 일반론에 그치거나 사실과 다를 수 있다 (환각)
              - 이 대비가 RAG의 존재 이유다.
            """.textBlock()
        )
    }

    private fun preview(text: String): String {
        // trim 기준은 Java String.trim()과 같게 (코드값 ' ' 이하만 제거)
        val flat = text.replace(WHITESPACE, " ").trim { it <= ' ' }
        return if (flat.length <= 80) flat else flat.take(80) + " ..."
    }
}

private val WHITESPACE = Regex("\\s+")
