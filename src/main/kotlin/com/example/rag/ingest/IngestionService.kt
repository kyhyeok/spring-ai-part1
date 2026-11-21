package com.example.rag.ingest

import org.slf4j.LoggerFactory
import org.springframework.ai.document.Document
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

/**
 * 적재 파이프라인:  Reader -> Splitter -> VectorStore.add()
 * 교안 10장 5, 8, 9, 10절에 해당한다.
 */
@Service
class IngestionService(
    private val vectorStore: VectorStore,
    @param:Value("classpath:docs/company-policy.txt") private val policyDoc: Resource,
    @param:Value("classpath:docs/sample-contract.pdf") private val contractDoc: Resource,
    @param:Value("\${rag.chunk-size:500}") private val chunkSize: Int,
) {

    /** 기본 샘플 문서 적재 */
    fun ingestSample(): IngestResult {
        // 1) 로딩 : 파일 1개 = Document 1개

        // 메타데이터 설계 (교안 10장) - 적재 시점에 넣지 않으면 검색 시점에 쓸 수 없다

        val docs: List<Document> = TODO("실습: 1) 로딩")

        // 2) 청킹
        val chunks: List<Document> = TODO("실습: 2) 청킹")

        // 3) 임베딩 + 저장 (add() 내부에서 EmbeddingModel 호출)


        log.info(
            "적재 완료: 원본 {}건 -> 청크 {}건 (chunkSize={})",
            docs.size, chunks.size, chunkSize,
        )
        return IngestResult(docs.size, chunks.size, chunkSize)
    }

    /** PDF 적재 - 페이지 단위 Document + page 메타데이터 자동 부여 */
    fun ingestPdf(): IngestResult? {


//        log.info("PDF 적재 완료: {} - 페이지 {}건 -> 청크 {}건", "sample-contract.pdf", docs.size, chunks.size)
//        return IngestResult(docs.size, chunks.size, chunkSize)
        return null
    }

    private fun split(docs: List<Document>): List<Document> {
        val splitter = TokenTextSplitter.builder()
            .withChunkSize(chunkSize)               // 기본 텍스트 분할 토큰 크기
            .withMinChunkSizeChars(100)             // 최소 문자(Char) 단위 크기
            .withMinChunkLengthToEmbed(5)           // 임베딩할 최소 길이
            .withMaxNumChunks(10000)                // 최대 생성 가능 Chunk 수
            .withKeepSeparator(true)                // 구분자(Separator) 유지 여부
            .build()

        return splitter.apply(docs)   // 메타데이터는 청크에 자동 승계된다
    }

    data class IngestResult(val documents: Int, val chunks: Int, val chunkSize: Int)

    companion object {
        private val log = LoggerFactory.getLogger(IngestionService::class.java)
    }
}
