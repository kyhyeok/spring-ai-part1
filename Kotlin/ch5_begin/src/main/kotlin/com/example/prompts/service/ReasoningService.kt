package com.example.prompts.service

import com.example.prompts.dto.BeforeAfterResponse
import org.springframework.stereotype.Service

/**
 * Chain-of-Thought — "단계별로 생각한 뒤 답하세요"
 *
 * 적용 기준: 조건 3개 이상이 얽힌 판단이면 CoT 시도.
 * 주의: 출력 토큰 증가 = 비용·지연 증가. 단순 과제엔 오히려 군더더기.
 *      최신 추론 특화 모델은 내부적으로 유사 과정을 수행하므로 모델에 따라 효과 차이가 있다.
 */
@Service
class ReasoningService(
    private val runner: PromptRunner,
) {

    /** CoT 없이 — 결론만 요구 */
    fun withoutCot(problem: String): String? = null

    /** CoT 적용 — 한 줄 추가로 추론 품질을 높인다 */
    fun withCot(problem: String): String? = null

    /** 유/무 비교 — 중간 사고 과정이 드러나 검증 가능성이 올라가는 것을 확인 */
    fun compare(problem: String): BeforeAfterResponse? {
        val withoutCotPrompt = "$problem\n답만 알려주세요."
        val withCotPrompt = "$problem\n단계별로 검토한 뒤 결론을 내세요."

        return null
    }

    companion object {
        /** 슬라이드 노트의 "나이·조건 계산 같은 간단한 다단계 문제" — 조건 3개가 얽힌 판단 */
        val SAMPLE_PROBLEM: String = """
            어느 놀이공원의 할인 규정입니다.
            - 만 13세 미만은 어린이 요금(8,000원)
            - 만 65세 이상은 경로 요금(6,000원)
            - 그 외는 성인 요금(15,000원)
            - 4인 이상 동반 시 전체 금액에서 10% 할인
            - 화요일은 성인 요금만 2,000원 추가 할인

            화요일에 만 11세 1명, 만 40세 2명, 만 67세 1명이 함께 입장했습니다.
            총 결제 금액은 얼마입니까?
            """.textBlock()
    }
}
