package com.example.prompts.service

import com.example.prompts.dto.InjectionDefenseResult
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.util.regex.Pattern

/**
 * Prompt Injection — Prompt는 공격 표면(Attack Surface).
 * 3중 방어 구조 — Defense in Depth.
 *
 *   1차 입력 검증        — 요청이 모델에 닿기 전
 *   2차 시스템 프롬프트 방어 — 모델 스스로 버티게
 *   3차 출력 검증        — 새어 나가기 전 마지막 문
 *
 * 여기서는 세 겹의 '원리'를 최소 구현으로 보여준다.
 * Advisor 체인으로 만드는 프로덕션 가드레일은 Section 14에서 이어진다.
 */
@Service
class PromptInjectionDemoService(
    private val runner: PromptRunner,
    /** 2차 — 방어 규칙이 포함된 시스템 프롬프트 (외부 파일, @param: — 생성자 파라미터에만 주입) */
    @param:Value("classpath:/prompts/system-hardened-refund-bot.st") private val hardenedResource: Resource,
) {

    /**
     * 공격 문자열 하나를 세 겹의 방어에 차례로 통과시키고, 각 층의 판정을 그대로 반환한다.
     *
     * @param userInput 수강생이 직접 만들어 보는 공격문.
     */
    fun run(userInput: String): InjectionDefenseResult {

        // ── 방어 없는 봇 (비교군) ─────────────────────────────
        // begin 코드의 빈자리: 시스템 프롬프트가 아직 비어 있어, Java와 같이 여기서 예외가 난다
        val undefended = runner.systemAndUser(
            checkNotNull(UNDEFENDED_SYSTEM) { "UNDEFENDED_SYSTEM이 비어 있습니다" },
            userInput,
        )

        // ── 1차. 입력 검증 ────────────────────────────────────
        val matched = buildList {
            SUSPICIOUS_INPUT.filter { it.matcher(userInput).find() }.mapTo(this) { it.pattern() }
            if (userInput.length > MAX_INPUT_LENGTH) {
                add("입력 길이 초과 (> ${MAX_INPUT_LENGTH}자)")
            }
        }

        if (matched.isNotEmpty()) {
            // 모델에 닿기 전에 차단 — 토큰 비용도 발생하지 않는다
            return InjectionDefenseResult(
                userInput, undefended,
                true, matched,
                null,
                false, emptyList(),
                BLOCK_MESSAGE,
            )
        }

        // ── 2차. 시스템 프롬프트 방어 ─────────────────────────
        val hardenedSystem = PromptTemplate(hardenedResource).template
        val hardenedAnswer = runner.systemAndUser(hardenedSystem, userInput)

        // ── 3차. 출력 검증 ────────────────────────────────────
        // 응답이 null이면 Java는 contains()에서 NPE(500) — 검사할 조각이 있을 때만 같은 지점에서 실패시킨다
        val violations = FORBIDDEN_OUTPUT_FRAGMENTS
            .filter { checkNotNull(hardenedAnswer) { "모델 응답이 없습니다" }.contains(it) }
            .map { "시스템 프롬프트 조각 유출: \"$it\"" }
        val outputBlocked = violations.isNotEmpty()

        return InjectionDefenseResult(
            userInput, undefended,
            false, emptyList(),
            hardenedAnswer,
            outputBlocked, violations,
            if (outputBlocked) BLOCK_MESSAGE else hardenedAnswer,
        )
    }

    companion object {
        /** 방어 없는 봇의 시스템 프롬프트 */
        private val UNDEFENDED_SYSTEM: String? = null

        /** 1차 — 의심 패턴 (슬라이드의 "지시 무시" 등). Java와 같은 플래그를 쓰려고 Kotlin Regex 대신 Pattern 사용 */
        private val SUSPICIOUS_INPUT: List<Pattern> = listOf(
            Pattern.compile("""(이전|위의?|모든)\s*지시.*(무시|잊)""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""ignore\s+(all\s+)?(previous|above)\s+instructions?""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""(시스템|system)\s*(프롬프트|prompt).*(출력|공개|알려|보여)""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""너는\s*이제.*(제한이?\s*없|아무거나)""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""(역할|role)\s*(을|를)?\s*(변경|바꿔)""", Pattern.CASE_INSENSITIVE),
        )

        /** 1차 — 입력 길이 제한 */
        private const val MAX_INPUT_LENGTH = 1000

        /** 3차 — 응답에 섞이면 안 되는 조각 */
        private val FORBIDDEN_OUTPUT_FRAGMENTS: List<String> = emptyList()

        private val BLOCK_MESSAGE: String? = null
    }
}
