package com.example.rag.eval

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * 평가 대상 RAG 서비스.
 *
 * Advisor 대신 직접 검색하는 이유: 평가에는 "그때 사용된 근거 문서"가 반드시 필요하기 때문.
 * (교안 8장 7절 - EvaluationRequest 에 docs 를 넘겨야 한다)
 */
@Service
class RagService(
    builder: ChatClient.Builder,
    private val vectorStore: VectorStore,
    @param:Value("\${rag.top-k:4}") private val topK: Int,
    @param:Value("\${rag.similarity-threshold:0.0}") private val threshold: Double,
    @param:Value("\${rag.active-chunk-size:500}") private val activeChunkSize: Int,
) {

    private val chatClient: ChatClient = builder.build()

    /** 검색 + 생성. 사용된 근거를 함께 반환한다. */
    fun ask(question: String): RagResult {
        val searchRequestBuilder: SearchRequest.Builder? = null

        val docs: List<Document>? = null

        if (docs!!.isEmpty()) {
            return RagResult(question, "제공된 문서에서 확인할 수 없습니다.", emptyList(), 0)
        }

        // getText()가 @Nullable — Java Collectors.joining처럼 텍스트가 없으면 "null"로 이어 붙인다
        val context = docs.map { it.text }.joinToString("\n---\n")

        val answer = chatClient.prompt()
            .system(SYSTEM_PROMPT)
            .user { u ->
                u.text("[근거]\n{ctx}\n\n[질문]\n{q}")
                    .param("ctx", context)
                    .param("q", question)
            }
            .call()
            .content()

        val contextTokensApprox = context.length / 2   // 한국어 근사치 (실측 대용)
        return RagResult(question, answer, docs, contextTokensApprox)
    }

    // answer: Spring AI content()가 @Nullable이라 null 허용
    data class RagResult(
        val question: String,
        val answer: String?,
        val evidences: List<Document>,
        val contextTokensApprox: Int,
    )
}

// Java 텍스트 블록과 글자 단위로 같게: 닫는 """가 마지막 줄 끝에 붙어 있어 끝 줄바꿈이 없다
private val SYSTEM_PROMPT = """
    너는 사내 규정 안내 도우미다.
    아래 [근거]에 있는 내용만 사용해 한국어로 간결하게 답하라.
    근거에 없는 내용은 절대 추측하지 말고
    정확히 "제공된 문서에서 확인할 수 없습니다."라고만 답하라.
    """.trimIndent()
