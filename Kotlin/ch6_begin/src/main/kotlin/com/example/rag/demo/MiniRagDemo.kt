package com.example.rag.demo

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.reader.TextReader
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.SimpleVectorStore
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
        val reader = TextReader(policyDoc)
        reader.customMetadata["source"] = "company-policy.txt"
        val docs = reader.get()

        val splitter = TokenTextSplitter.builder()
            .withChunkSize(400)             // 기본 텍스트 분할 토큰 크기
            .withMinChunkSizeChars(100)     // 최소 문자 단위 크기
            .withMinChunkLengthToEmbed(5)   // 임베딩할 최소 길이
            .withMaxNumChunks(10_000)       // 최대 생성 가능 청크 수
            .withKeepSeparator(true)        // 구분자 유지 여부
            .build()

        val chunks = splitter.apply(docs)
        val vectorStore = SimpleVectorStore.builder(embeddingModel).build()
        vectorStore.add(chunks)
        println("      원본 %d건 -> 청크 %d건 적재 완료%n".format(docs.size, chunks.size))

        val question = "연차 휴가는 며칠이고 언제까지 사용해야 하나요?"

        // ---------- 2. 검색 ----------
        println("[2/3] 유사도 검색 (LLM 호출 전 - 근거 확인)")
        println("      질문: $question")
        /* 구현 */
        val found = vectorStore.similaritySearch(
            SearchRequest.builder().query(question).topK(3).build()
        )

        found.forEachIndexed { i, d ->
            println(
                "      근거 %d) [%s] %s".format(
                    i + 1,
                    d.metadata.getOrDefault("source", "-"),
                    preview(d.requiredText),
                )
            )
        }
        println()

        // ---------- 3. 생성 ----------
        println("[3/3] 근거를 주입해 답변 생성")
        /* 구현 */
        val context = found.joinToString("\n----\n") { it.requiredText }

        // content()는 @Nullable이라 String? (Java와 같이 null이면 "null"로 출력)
        val withRag: String? = chatClient.prompt()
            .system(
                """
                너는 사내 규정 안내 도우미입니다.
                아래 [근거]에 있는 내용만 사용해서 한국어로 간결하게 답하세요.
                근거에 없으면 "제공된 문서에서 확인할 수 없습니다"라고 답하세요.
                """.textBlock()
            )
            .user { u ->
                u.text("[근거]\n{ctx}\n\n[질문]\n{q}")
                    .param("ctx", context)
                    .param("q", question)
            }
            .call()
            .content()

        val withoutRag: String? = chatClient.prompt()
            .user(question)
            .call()
            .content()

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
