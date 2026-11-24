package com.example.rag.ingest

import org.slf4j.LoggerFactory
import org.springframework.ai.document.Document
import org.springframework.ai.reader.TextReader
import org.springframework.ai.reader.pdf.PagePdfDocumentReader
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig
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
        val reader = TextReader(policyDoc)
        // 메타데이터 설계 (교안 10장) - 적재 시점에 넣지 않으면 검색 시점에 쓸 수 없다
        reader.customMetadata.putAll(
            mapOf(
                "source" to "company-policy.txt",
                "category" to "인사규정",
                "department" to "인사팀",
                "updated" to "2026-11-24",
            ),
        )
        val docs = reader.get()

        // 2) 청킹
        val chunks = split(docs)

        // 3) 임베딩 + 저장 (add() 내부에서 EmbeddingModel 호출)
        vectorStore.add(chunks)

        log.info(
            "적재 완료: 원본 {}건 -> 청크 {}건 (chunkSize={})",
            docs.size, chunks.size, chunkSize,
        )
        return IngestResult(docs.size, chunks.size, chunkSize)
    }

    /** PDF 적재 - 페이지 단위 Document + page 메타데이터 자동 부여 */
    fun ingestPdf(): IngestResult {
        val reader = PagePdfDocumentReader(
            contractDoc,
            PdfDocumentReaderConfig.builder()
                .withPagesPerDocument(1)
                .build(),
        )

        val docs = reader.get()

        // Java는 `d.getText() == null && d.getText().isBlank()`라 텍스트가 있으면 항상 0(경고가 뜨지 않음), null이면 NPE다
        // (PagePdfDocumentReader는 null 텍스트를 만들지 않음). 의도대로 "null이거나 공백"을 센다.
        // 응답은 같고 경고 로그만 다르다. 공백 판정은 Java String.isBlank()와 같은 Character.isWhitespace
        val emptyPages = docs.count { d -> d.text.let { it == null || it.all(Character::isWhitespace) } }

        if (emptyPages > 0) {
            log.warn("경고: 텍스트가 비어 있는 페이지 {}건 - 스캔본 PDF일 수 있습니다. (OCR 필요)", emptyPages)
        }

        docs.forEach { it.metadata["source"] = "sample-contract.pdf" }
        val chunks = split(docs)
        vectorStore.add(chunks)

        log.info("PDF 적재 완료: {} - 페이지 {}건 -> 청크 {}건", "sample-contract.pdf", docs.size, chunks.size)
        return IngestResult(docs.size, chunks.size, chunkSize)
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
