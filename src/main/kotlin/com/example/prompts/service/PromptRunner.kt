package com.example.prompts.service

import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.stereotype.Component

/**
 * 모든 실습이 공통으로 쓰는 호출 유틸.
 * 호출마다 토큰 사용량을 로그로 남긴다 — S16의 "시스템 프롬프트 길이 = 고정비"를 수치로 확인하기 위함.
 */
@Component
class PromptRunner(builder: ChatClient.Builder) {

    // defaultSystem을 지정하지 않는다 — 시스템 프롬프트의 유무·내용 차이가 이 섹션의 실습 대상이기 때문.
    private val chatClient: ChatClient = builder.build()

    fun client(): ChatClient = chatClient

    /** user 채널만 사용 */
    fun user(userText: String): String? = text(chatResponse { it.user(userText) })

    /** system + user — S6의 역할 분리 */
    fun systemAndUser(systemText: String, userText: String): String? =
        text(chatResponse { it.system(systemText).user(userText) })

    /** 토큰 수까지 필요한 경우 */
    fun userWithTokens(userText: String): Result = result(chatResponse { it.user(userText) })

    fun systemAndUserWithTokens(systemText: String, userText: String): Result =
        result(chatResponse { it.system(systemText).user(userText) })

    // 요청 구성만 호출부마다 다르고, 호출과 응답 확인은 같으므로 한 곳에 모은다.
    // chatResponse()는 @Nullable — Java는 다음 단계에서 NPE(500), Kotlin은 같은 500에 원인 메시지를 남긴다
    private fun chatResponse(
        configure: (ChatClient.ChatClientRequestSpec) -> ChatClient.ChatClientRequestSpec,
    ): ChatResponse =
        checkNotNull(configure(chatClient.prompt()).call().chatResponse()) { "ChatResponse가 없습니다" }

    private fun result(response: ChatResponse): Result = Result(text(response), totalTokens(response))

    /*
     * ChatResponse는 텍스트 외에 토큰 사용량, 정상 종료 여부, 메타데이터 등의 정보도 담는다
     */
    private fun text(response: ChatResponse): String? {
        val usage = response.metadata.usage

        log.debug(
            "model={} tokens(prompt/completion/total)={}/{}/{}",
            response.metadata.model,
            usage.promptTokens,
            usage.completionTokens,
            usage.totalTokens,
        )

        // getResult()는 @Nullable — Java는 여기서 NPE(500), Kotlin은 같은 500에 원인 메시지를 남긴다
        return checkNotNull(response.result) { "ChatResponse에 결과(Generation)가 없습니다" }.output.text
    }

    // Spring AI 2.x의 Usage는 @NullMarked 패키지라 totalTokens가 non-null (사용량이 없으면 0)
    private fun totalTokens(response: ChatResponse): Long = response.metadata.usage.totalTokens.toLong()

    data class Result(val answer: String?, val totalTokens: Long)

    companion object {
        private val log = LoggerFactory.getLogger(PromptRunner::class.java)
    }
}

/**
 * Java 텍스트 블록(닫는 `"""`가 본문과 같은 들여쓰기로 따로 줄에 있는 형태)과 같은 문자열을 만든다.
 * Java처럼 공통 들여쓰기와 줄마다 끝 공백을 걷어 내고 끝에 줄바꿈을 붙여, 프롬프트 문구를 Java와 글자 단위로 맞춘다.
 *
 * 값은 `$변수`로 직접 넣지 말고 `%s` 자리에 format()으로 넣는다.
 * 여러 줄 값을 템플릿으로 넣으면 그 값의 들여쓰기까지 trimIndent() 계산에 섞여 들여쓰기가 남는다.
 */
internal fun String.textBlock(): String =
    // 줄 끝 공백 판정은 Java와 같은 Character.isWhitespace (Kotlin isWhitespace는 NBSP까지 지운다)
    trimIndent().lines().joinToString("\n") { line -> line.trimEnd { Character.isWhitespace(it) } } + "\n"
