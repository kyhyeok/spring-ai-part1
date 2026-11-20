package com.example.rag.demo

import org.springframework.ai.document.Document
import org.springframework.ai.reader.TextReader
import org.springframework.ai.transformer.splitter.TokenTextSplitter
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
        val source = TextReader(policyDoc).get() // source = 1
        val totalChars = source.sumOf { it.requiredText.length }
        println("원본 문서: %d건, 총 %,d자%n".format(source.size, totalChars))

        for (chunkSize in intArrayOf(200, 500, 1000)) {
            val splitter = TokenTextSplitter.builder()
                .withChunkSize(chunkSize)       // 기본 텍스트 분할 토큰 크기
                .withMinChunkSizeChars(100)     // 최소 문자 단위 크기
                .withMinChunkLengthToEmbed(5)   // 임베딩할 최소 길이
                .withMaxNumChunks(10_000)       // 최대 생성 가능 청크 수
                .withKeepSeparator(true)        // 구분자 유지 여부
                .build()

            val chunks = splitter.apply(source)

            // Java IntStream.average().orElse(0)처럼 청크가 없으면 0 (Kotlin average()는 NaN)
            val avgLen = if (chunks.isEmpty()) 0.0 else chunks.map { it.requiredText.length }.average()
            println("[청크 크기 %4d 토큰] -> %2d개 청크, 평균 %.0f자".format(chunkSize, chunks.size, avgLen))
            println(" 첫 청크 미리보기")
            println("    ${preview(chunks[0].requiredText)}")

            if (chunks.size > 1) {
                println(" 두번째 청크 미리보기")
                println("    ${preview(chunks[1].requiredText)}")
            }
            println()
        }

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

/**
 * Spring AI 2.x `Document.getText()`는 `@Nullable`(미디어 문서는 텍스트가 없음)이다.
 * TextReader·TokenTextSplitter가 만든 문서는 항상 텍스트가 있으므로, 없으면 원인 메시지와 함께 실패한다
 * (Java는 같은 자리에서 NPE).
 */
private val Document.requiredText: String
    get() = checkNotNull(text) { "텍스트가 없는 Document: $id" }
