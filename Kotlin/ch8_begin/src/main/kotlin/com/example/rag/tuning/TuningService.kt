package com.example.rag.tuning

import org.slf4j.LoggerFactory
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.util.regex.Pattern

/**
 * 튜닝 실험 서비스 (교안 8장 2~3절)
 *
 * 핵심: 청크 크기별로 chunkSize 메타데이터를 부여해 "논리적 컬렉션"을 분리한다.
 *       -> 같은 테이블에 적재하되 filterExpression 으로 분리 검색하여 공정 비교가 가능하다.
 */
@Service
class TuningService(
    private val vectorStore: VectorStore,
    @param:Value("classpath:docs/company-policy.txt") private val policyDoc: Resource,
    @Value("\${rag.chunk-sizes:200,500,1000}") chunkSizesCsv: String,
) {

    // Java String.split(",")·trim()과 같은 규칙: 끝의 빈 항목은 버리고(Pattern.split), 코드값 ' ' 이하만 잘라낸다
    private val chunkSizes: List<Int> = COMMA.split(chunkSizesCsv).map { it.trim { c -> c <= ' ' }.toInt() }

    /** 청크 크기별로 각각 적재한다 (chunkSize 메타데이터로 구분) */
    fun ingestAll(): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        for (size in chunkSizes) {


//            val chunks = TokenTextSplitter.builder()
//                .withChunkSize(size)                    // 기본 텍스트 분할 토큰 크기
//                .withMinChunkSizeChars(100)             // 최소 문자(Char) 단위 크기
//                .withMinChunkLengthToEmbed(5)           // 임베딩할 최소 길이
//                .withMaxNumChunks(10000)                // 최대 생성 가능 Chunk 수
//                .withKeepSeparator(true)                // 구분자(Separator) 유지 여부
//                .build()
//                .apply(docs)

            // 적재


//            log.info("적재 완료 [chunkSize={}] -> {}건 (평균 {}자)", size, chunks.size, Math.round(avgLen))
        }
        return result
    }

    /** 청크 크기별 검색 결과를 나란히 비교한다 (교안 6장 2절 실습) */
    fun compare(question: String, topK: Int): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        for (size in chunkSizes) {

        }

        result["_hint"] = "작은 청크는 정밀하지만 문맥이 끊기고, 큰 청크는 문맥이 온전하나 노이즈가 섞인다"
        return result
    }

    /** 검색 파라미터 실험 (교안 8장 3절) */
    fun search(
        question: String,
        topK: Int,
        threshold: Double,
        chunkSize: Int?,
        category: String?,
    ): Map<String, Any> {
        val req: SearchRequest.Builder? = null

        val filters = mutableListOf<String>()


        val found = vectorStore.similaritySearch(req!!.build())

        // mapOf는 JSON 키가 선언 순서로 고정된다 (Java Map.of는 실행마다 순서가 다를 수 있음, 내용은 같음)
        return mapOf(
            "question" to question,
            "params" to mapOf(
                "topK" to topK,
                "threshold" to threshold,
                "chunkSize" to (chunkSize ?: "-"),
                "category" to (category ?: "-"),
            ),
            "hitCount" to found.size,
            "hits" to found.map { d ->
                mapOf(
                    "score" to round(d.score),
                    // getText()가 @Nullable — 텍스트가 없는 Document면 원인 메시지와 함께 실패 (Java는 같은 자리에서 NPE)
                    "text" to preview(checkNotNull(d.text) { "텍스트가 없는 Document: ${d.id}" }),
                )
            },
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(TuningService::class.java)
    }
}

private val COMMA: Pattern = Pattern.compile(",")
private val WHITESPACE = Regex("\\s+")

// roundToLong()과 달리 NaN이면 0 (Java Math.round와 같음)
private fun round(score: Double?): Double = if (score == null) 0.0 else Math.round(score * 10000) / 10000.0

private fun preview(text: String): String {
    // trim 기준은 Java String.trim()과 같게 (코드값 ' ' 이하만 제거)
    val flat = text.replace(WHITESPACE, " ").trim { it <= ' ' }
    return if (flat.length <= 160) flat else flat.take(160) + " ..."
}
