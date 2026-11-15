package com.example.prompts.service

import com.example.prompts.dto.BeforeAfterResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service

/**
 * 프롬프트가 품질을 결정 / 역할 구조 / 좋은 System Prompt의 4요소.
 */
@Service
class PromptQualityService(
    private val runner: PromptRunner,
    /** 4요소 시스템 프롬프트 — 외부 파일로 관리 (@param: — 생성자 파라미터에만 주입) */
    @param:Value("classpath:/prompts/system-customer-center.st") private val customerCenterResource: Resource,
) {

    /**
     * 같은 모델, 같은 비용. 문장만 바꿔도 결과 품질이 달라진다.
     * Before: "이 기사 요약해줘"  →  After: 역할·형식·제약 명시
     */
    fun summaryBeforeAfter(article: String): BeforeAfterResponse {
        val before = "이 기사 요약해줘\n\n$article"

        val after = """
            당신은 경제 뉴스 에디터입니다.
            아래 기사를 핵심 사실 중심으로
            3개의 불릿, 각 40자 이내로 요약하세요.
            의견이나 전망은 포함하지 마세요.

            기사:
            %s
            """.textBlock().format(article)

        val b = runner.userWithTokens(before)
        val a = runner.userWithTokens(after)

        return BeforeAfterResponse(
            "프롬프트가 품질을 확인하는 예제",
            "Before - 막연한 지시", before, b.answer, b.totalTokens,
            "After - 역할/형식/제약 명시", after, a.answer, a.totalTokens,
        )
    }

    /**
     * 역할 구조. 사용자 입력은 user 채널로만 전달한다.
     * 슬라이드 코드 그대로: system("당신은 Spring 전문가입니다. 코드 예시와 함께 한국어로 답하세요.")
     */
    fun roleSeparated(question: String): String? =
        runner.client().prompt()
            .system(SPRING_EXPERT_SYSTEM)
            .user(question)
            .call().content()

    /**
     * 안티패턴 비교 — 사용자 입력을 system에 섞어 넣으면 어떻게 되는가.
     * 인젝션 데모에서 이 분리가 왜 보안의 기초인지 회수된다.
     */
    fun roleSeparationAntiPattern(question: String): BeforeAfterResponse {
        // ❌ 안티패턴: 사용자 입력을 시스템 메시지에 이어 붙임
        val mixed = "$SPRING_EXPERT_SYSTEM \n$question"
        val bad = runner.userWithTokens(mixed)

        // ✅ 정상: 채널 분리
        val good = runner.systemAndUserWithTokens(SPRING_EXPERT_SYSTEM, question)

        return BeforeAfterResponse(
            "역할 구조 (System/User/Assistant)",
            "안티패턴 - 하나의 프롬프트로 전달", mixed, bad.answer, bad.totalTokens,
            "프롬프트 분리 - System / User", "[System] String 전문가 규칙, [User] $question",
            good.answer, good.totalTokens,
        )
    }

    /**
     * 좋은 System Prompt의 4요소(역할·형식·제약·톤).
     * 고객센터 봇 예시를 외부 파일(system-customer-center.st)에서 읽어 {company}만 바인딩한다.
     */
    fun customerCenterBot(company: String, question: String): String? = null

    companion object {
        /** 슬라이드에서 사용하는 예시 기사 (수강생이 다른 기사로 바꿔 실험) */
        val SAMPLE_ARTICLE: String = """
            한국은행이 기준금리를 연 3.00%로 동결했다. 3회 연속 동결이다.
            금융통화위원회는 물가 상승률이 목표치에 근접했으나 가계부채 증가세와
            원/달러 환율 변동성을 고려해 신중한 기조를 유지하기로 했다.
            시장에서는 하반기 인하 가능성을 점치고 있으나, 총재는 기자회견에서
            "인하 시점을 예단하기 이르다"고 말했다. 이날 코스피는 0.4% 상승 마감했다.
            """.textBlock()

        // 역할 분리 예제 3곳(분리 호출·안티패턴·채널 분리)이 같은 시스템 문구를 쓴다
        private const val SPRING_EXPERT_SYSTEM = "당신은 Spring 전문가입니다. 코드 예시와 함께 한국어로 답하세요."
    }
}
