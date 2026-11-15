package com.hyeok.springai.services

import com.hyeok.springai.jev.JevClient
import com.hyeok.springai.model.Genre
import com.hyeok.springai.model.Movie
import com.hyeok.springai.model.ReviewAnalysis
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service

@Service
class MovieServiceImpl(
    builder: ChatClient.Builder,
    private val jevClient: JevClient,
) : MovieService {

    private val chatClient: ChatClient = builder.build()

    // 포맷 지시문은 호출할 때마다 같으므로 한 번만 만들어 재사용한다
    // (Java는 호출마다 BeanOutputConverter를 새로 만들어 스키마를 다시 생성한다)
    private val movieFormat: String = BeanOutputConverter(Movie::class.java).format

    override fun findMovie(title: String): Movie? =
        chatClient.prompt()
            .user { it.text("영화 '{title}'의 제목, 개봉연도, 감독을 알려줘").param("title", title) }
            .call()
            .entity(Movie::class.java)

    // Spring AI의 Kotlin 확장 entity<T>()는 반환형이 non-null이라, LLM이 `null`이라고 답해
    // 변환 결과가 null이면 예외(500)가 난다. Java처럼 null(빈 본문 200)을 그대로 돌려주도록
    // ParameterizedTypeReference를 직접 넘긴다.
    override fun findMoviesByDirector(director: String, count: Int): List<Movie>? =
        chatClient.prompt()
            .user {
                it.text("{director} 감독의 대표작 {count}편을 알려줘")
                    .param("director", director)
                    .param("count", count.toString())
            }
            .call()
            .entity(object : ParameterizedTypeReference<List<Movie>>() {})

    /* 문자열로 JSON을 직접 요청하는 방식
     * ```json ... ``` */
    override fun askRawJsonTrap(title: String): String? =
        chatClient.prompt()
            .user {
                it.text("영화 '{title}'의 정보를 title, year, director 필드를 가진 JSON으로 알려줘")
                    .param("title", title)
            }
            .call()
            .content()
            .also { log.info("TRAP RAW >>> {}", it) }

    /* 프롬프트에 마크다운 금지를 명시 */
    override fun askRawJsonFixed(title: String): String? =
        chatClient.prompt()
            .user {
                // Java 텍스트 블록은 끝에 줄바꿈이 붙으므로 같은 프롬프트가 되도록 "\n"을 더한다
                it.text(
                    """
                    영화 '{title}'의 정보를 title, year, director 필드를 가진 JSON으로 알려줘.
                    Response in JSON format without markdown tags.
                    """.trimIndent() + "\n",
                ).param("title", title)
            }
            .call()
            .content()
            .also { log.info("FIXED RAW >>> {}", it) }

    /**
     * [S10 강의 노트용] entity()가 프롬프트 뒤에 자동으로 붙이는
     * 포맷 지시문(JSON 스키마)을 문자열로 반환합니다.
     * "마법이 아니라 자동화"임을 보여줄 때 사용하세요.
     */
    override fun describeFormat(): String = movieFormat

    /**
     * [S10-대응②와 비교] LLM + entity() — 스키마를 "부탁"하는 방식.
     * 대부분 잘 되지만 enum에 없는 값이 오면 파싱 예외가 날 수 있습니다.
     */
    override fun analyzeReviewWithLlm(review: String): ReviewAnalysis? =
        chatClient.prompt()
            .user {
                // Java 텍스트 블록은 끝에 줄바꿈이 붙으므로 같은 프롬프트가 되도록 "\n"을 더한다
                it.text(
                    """
                    다음 영화 리뷰를 분석해줘.
                    sentiment는 0(매우 부정) ~ 4(매우 긍정), confidence는 null로 둬.
                    리뷰: {review}
                    """.trimIndent() + "\n",
                ).param("review", review)
            }
            .call()
            .entity(ReviewAnalysis::class.java)

    /**
     * [S10-대응③] Jev — 보기 중에서만 고르므로 형식 오류가 구조적으로 불가능.
     * criteria를 enum에서 만들기 때문에 응답은 항상 Genre.valueOf()로 변환됩니다.
     */
    override fun analyzeReviewWithJev(review: String): ReviewAnalysis {
        // 선언 순서를 지키는 Map (associate는 LinkedHashMap을 만든다)
        val genreCriteria: Map<String, String> = Genre.entries.associate { it.name to it.description }

        val answers = jevClient.decide(
            review,
            mapOf(
                "genre" to mapOf(
                    "type" to "choice",
                    "instructions" to "리뷰가 다루는 영화의 장르는?",
                    "criteria" to genreCriteria,
                ),
                "sentiment" to mapOf(
                    "type" to "score",
                    "instructions" to "리뷰 작성자의 영화 평가는?",
                    "criteria" to listOf("매우 부정", "부정", "보통", "긍정", "매우 긍정"),
                ),
                "spoiler" to mapOf(
                    "type" to "noul",
                    "instructions" to "리뷰가 결말이나 반전을 드러내는가?",
                ),
            ),
        )

        // 질문 타입에 맞는 값이 없으면 Java는 NPE, Kotlin은 원인을 담은 예외 (둘 다 500)
        val genre = answers.getValue("genre")

        return ReviewAnalysis(
            genre = Genre.valueOf(checkNotNull(genre.choice) { "genre.choice 없음" }),
            sentiment = checkNotNull(answers.getValue("sentiment").score) { "sentiment.score 없음" },
            spoiler = checkNotNull(answers.getValue("spoiler").noul) { "spoiler.noul 없음" } >= 0.5,
            confidence = genre.confidence,
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(MovieServiceImpl::class.java)
    }
}
