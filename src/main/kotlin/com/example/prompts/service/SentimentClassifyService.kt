package com.example.prompts.service

import com.example.prompts.dto.BeforeAfterResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

/**
 * Zero-shot vs Few-shot — 예시를 줄 것인가.
 * Few-shot 실전 — 감성 분류. "예시가 곧 사양(Spec)"
 *
 * 시연 순서: Zero-shot으로 먼저 시켜 "긍정적인 리뷰로 보입니다. 왜냐하면..." 같은 장황한 응답을
 *          보여준 뒤, Few-shot으로 한 단어 출력에 수렴시킨다.
 */
@Service
class SentimentClassifyService(
    private val runner: PromptRunner,
    // @param: — 생성자 파라미터에만 붙인다. 기본 대상(param-property)이면 필드에도 붙어 Spring이 final 필드에 한 번 더 주입한다
    @param:Value("classpath:/prompts/few-shot-sentiment.st") private val fewShotResource: Resource,
) {

    /** Zero-shot — 지시만으로 수행 */
    fun zeroShot(review: String): String? = null

    /** Few-shot — 예시 2개로 출력 형식을 고정 (슬라이드 프롬프트 그대로) */
    fun fewShot(review: String): String? = null

    /** 두 방식을 나란히 — 출력이 예시의 형식에 수렴하는 것을 확인 */
    fun compare(review: String): BeforeAfterResponse? {
        // begin 코드의 빈자리: 프롬프트가 아직 비어 있어, Java와 같이 첫 호출에서 예외가 난다
        val zeroPrompt: String? = null
        val fewPrompt: String? = null

        val z = runner.userWithTokens(checkNotNull(zeroPrompt) { "zeroPrompt가 비어 있습니다" })
        val f = runner.userWithTokens(checkNotNull(fewPrompt) { "fewPrompt가 비어 있습니다" })

        return null
    }
}
