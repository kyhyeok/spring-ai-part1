package com.example.rag.api

import com.example.rag.ingest.IngestionService
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Section 7 실습 API
 *
 *  POST /api/rag/ingest       샘플 문서 적재
 *  GET  /api/rag/search       유사도 검색만 (LLM 호출 없음) - 적재 품질 검증용
 *  GET  /api/rag/ask          RAG 질의응답 (QuestionAnswerAdvisor)
 *  GET  /api/rag/ask-norag    RAG 미적용 답변 - 비교 실습용
 *
 * 응답 Map은 mapOf라 JSON 키가 선언 순서로 고정된다 (Java Map.of는 실행마다 순서가 다를 수 있음, 내용은 같음)
 */
@RestController
@RequestMapping("/api/rag")
class RagController(
    private val ingestionService: IngestionService,
    private val vectorStore: VectorStore,
    builder: ChatClient.Builder,
    @param:Value("\${rag.top-k:4}") private val defaultTopK: Int,
) {

    // QuestionAnswerAdvisor 적용
    private val ragChatClient: ChatClient = builder.clone()
        .defaultSystem(RAG_SYSTEM_PROMPT)
        .defaultAdvisors(
            QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().topK(defaultTopK).build())
                .build(),
        )
        .build()

    // 비교용 (RAG 없음)
    private val plainChatClient: ChatClient = builder.clone().build()

    @PostMapping("/ingest")
    fun ingest(): IngestionService.IngestResult = ingestionService.ingestSample()

    @PostMapping("/ingestPdf")
    fun ingestPdf(): IngestionService.IngestResult = ingestionService.ingestPdf()

    /**
     * LLM 없이 검색만 확인한다.
     * 교안 10장 10절 - "RAG가 이상하면 검색부터 본다"는 습관을 위한 엔드포인트.
     */
    @GetMapping("/search")
    fun search(
        @RequestParam q: String,
        @RequestParam(required = false) topK: Int?,
        @RequestParam(required = false) source: String?,
    ): List<Map<String, Any>> {
        // 위 코드를 아래와 같이 변환
        val requestBuilder = SearchRequest.builder()
            .query(q)
            .topK(topK ?: defaultTopK)

        // source가 전달된 경우 metadata 필터 적용
        // 공백 판정은 Java String.isBlank()와 같은 Character.isWhitespace (Kotlin isBlank()는 NBSP 등도 공백으로 본다)
        if (source != null && !source.all(Character::isWhitespace)) {
            requestBuilder.filterExpression("source == '$source'")
        }

        val results = vectorStore.similaritySearch(requestBuilder.build())

        // 결과 반환
        // Document는 메타데이터에 null 값을 허용하지 않으므로 ?: 는 Java getOrDefault와 같다
        return results.map { d ->
            mapOf(
                "source" to (d.metadata["source"] ?: "-"),
                "score" to (d.score ?: 0.0),
                "text" to preview(checkNotNull(d.text) { "텍스트가 없는 Document: ${d.id}" }),
            )
        }
    }

    /** RAG 질의응답 */
    @GetMapping("/ask")
    fun ask(@RequestParam q: String): Map<String, Any> {
        val answer = ragChatClient.prompt().user(q).call().content()
        return mapOf("mode" to "RAG", "question" to q, "answer" to checkAnswer(answer))
    }

    /** 비교용 - RAG 미적용 */
    @GetMapping("/ask-norag")
    fun askWithoutRag(@RequestParam q: String): Map<String, Any> {
        val answer = plainChatClient.prompt().user(q).call().content()
        return mapOf("mode" to "NO-RAG", "question" to q, "answer" to checkAnswer(answer))
    }

    private fun preview(text: String): String {
        // trim 기준은 Java String.trim()과 같게 (코드값 ' ' 이하만 제거)
        val flat = text.replace(WHITESPACE, " ").trim { it <= ' ' }
        return if (flat.length <= 200) flat else flat.take(200) + " ..."
    }

    // Java의 Map.of는 null 값을 받지 않아 답변이 null이면 NPE(500)가 난다. 같은 동작을 위해 null이면 예외
    private fun checkAnswer(answer: String?): String = checkNotNull(answer) { "답변이 없습니다" }
}

private val WHITESPACE = Regex("\\s+")

// Java 텍스트 블록과 글자 단위로 같게: 공통 들여쓰기를 지우고 끝 줄바꿈을 붙인다
private val RAG_SYSTEM_PROMPT = """
    너는 사내 규정 안내 도우미다.
    제공된 근거 문서의 내용만 사용해 한국어로 간결하게 답변해라.
    근거에 없는 내용은 추측하지 말고 "제공된 문서에서 확인할 수 없습니다"라고 답하라.
    답변 끝에 근거가 된 조항 번호를 표기하라.
    """.trimIndent() + "\n"
