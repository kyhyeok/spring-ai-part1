package com.example.rag.api

import com.example.rag.ingest.IngestionService
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.document.Document
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
 */
@RestController
@RequestMapping("/api/rag")
class RagController(
    private val ingestionService: IngestionService,
    private val vectorStore: VectorStore,
    builder: ChatClient.Builder,
    @param:Value("\${rag.top-k:4}") private val defaultTopK: Int,
) {

    private val ragChatClient: ChatClient = TODO("실습: ragChatClient 생성")      // QuestionAnswerAdvisor 적용
    private val plainChatClient: ChatClient = TODO("실습: plainChatClient 생성")  // 비교용 (RAG 없음)

    @PostMapping("/ingest")
    fun ingest(): IngestionService.IngestResult? = null

    @PostMapping("/ingestPdf")
    fun ingestPdf(): IngestionService.IngestResult? = null

    /**
     * LLM 없이 검색만 확인한다.
     * 교안 10장 10절 - "RAG가 이상하면 검색부터 본다"는 습관을 위한 엔드포인트.
     */
    @GetMapping("/search")
    fun search(
        @RequestParam q: String,
        @RequestParam(required = false) topK: Int?,
        @RequestParam(required = false) source: String?,
    ): List<Map<String, Any?>> {
        // 위 코드를 아래와 같이 변환
        val requestBuilder: SearchRequest.Builder? = null

        // source가 전달된 경우 metadata 필터 적용
        // 공백 판정은 Java String.isBlank()와 같은 Character.isWhitespace (Kotlin isBlank()는 NBSP 등도 공백으로 본다)
        if (source != null && !source.all(Character::isWhitespace)) {

        }

        val results: List<Document>? = null

        // 결과 반환
        return results!!.map { TODO("실습: 검색 결과 변환") }
    }

    /** RAG 질의응답 */
    @GetMapping("/ask")
    fun ask(@RequestParam q: String): Map<String, Any?>? {

        return null
    }

    /** 비교용 - RAG 미적용 */
    @GetMapping("/ask-norag")
    fun askWithoutRag(@RequestParam q: String): Map<String, Any?>? {

        return null
    }

    private fun preview(text: String): String {
        // trim 기준은 Java String.trim()과 같게 (코드값 ' ' 이하만 제거)
        val flat = text.replace(WHITESPACE, " ").trim { it <= ' ' }
        return if (flat.length <= 200) flat else flat.take(200) + " ..."
    }
}

private val WHITESPACE = Regex("\\s+")
