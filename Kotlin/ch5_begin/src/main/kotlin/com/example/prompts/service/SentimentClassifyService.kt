package com.example.prompts.service

import com.example.prompts.dto.BeforeAfterResponse
import org.springframework.ai.chat.prompt.PromptTemplate
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
    fun zeroShot(review: String): String? = runner.user(zeroShotPrompt(review))

    /** Few-shot — 예시 2개로 출력 형식을 고정 (슬라이드 프롬프트 그대로) */
    fun fewShot(review: String): String? = runner.user(fewShotPrompt(review))

    /** 두 방식을 나란히 — 출력이 예시의 형식에 수렴하는 것을 확인 */
    fun compare(review: String): BeforeAfterResponse {
        val zeroPrompt = zeroShotPrompt(review)
        val fewPrompt = fewShotPrompt(review)

        val z = runner.userWithTokens(zeroPrompt)
        val f = runner.userWithTokens(fewPrompt)

        return BeforeAfterResponse(
            "Zero-shot vs Few shot (감성 분류)",
            "Zero-shot - 지시만", zeroPrompt, z.answer, z.totalTokens,
            "Few-shot - 예시 2개", fewPrompt, f.answer, f.totalTokens,
        )
    }

    // 단독 호출(zeroShot·fewShot)과 비교(compare)가 같은 프롬프트를 쓰므로 만드는 곳을 하나로 둔다
    private fun zeroShotPrompt(review: String): String = "다음 리뷰의 감성을 분류하세요. \n\n리뷰: $review"

    private fun fewShotPrompt(review: String): String =
        PromptTemplate(fewShotResource).render(mapOf("review" to review))
}
