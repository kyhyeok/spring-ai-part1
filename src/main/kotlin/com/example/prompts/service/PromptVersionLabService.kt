package com.example.prompts.service

import com.example.prompts.dto.PromptVersionResult
import org.springframework.stereotype.Service

/**
 * 실습 — 같은 과제, 4개의 Prompt.
 *
 *   V1 기본        "이 기사 요약해줘"
 *   V2 + 역할      경제 뉴스 에디터 페르소나
 *   V3 + 형식·제약  3불릿 · 40자 · 의견 금지
 *   V4 + Few-shot  잘된 요약 예시 2개 제공
 *
 * 미션: 동일 기사로 V1→V4를 각 3회 실행하고
 *      ① 형식 일관성 ② 핵심 포착 ③ 분량 준수 세 기준으로 비교표를 작성.
 *      (1회 비교는 운이다 — temperature 0.3 고정, application.yml 참조)
 */
@Service
class PromptVersionLabService(
    private val runner: PromptRunner,
) {

    fun run(article: String, runsPerVersion: Int): PromptVersionResult {
        val runs = runsPerVersion.coerceIn(1, 3)   // 과금 방어: 최대 3회

        val versions = listOf(
            execute("V1", "기본", v1(article), runs),
            execute("V2", "+역할", v2(article), runs),
            execute("V3", "+형식/제약", v3(article), runs),
            execute("V4", "+Few-shot", v4(article), runs),
        )

        return PromptVersionResult(article, runs, versions)
    }

    private fun execute(id: String, label: String, prompt: String, runs: Int): PromptVersionResult.Version {
        // Java는 회차마다 answer.length()를 구하므로 응답이 null이면 그 회차에서 NPE(500) — 같은 지점에서 실패시킨다
        val answers = List(runs) { checkNotNull(runner.user(prompt)) { "모델 응답이 없습니다" } }

        return PromptVersionResult.Version(id, label, prompt, answers, answers.map { it.length })
    }

    // ── V1. 기본 ────────────────────────────────────────────────
    private fun v1(article: String): String = "이 기사 요약해줘\n\n$article"

    // ── V2. + 역할 (경제 뉴스 에디터 페르소나) ──────────────────
    private fun v2(article: String): String = """
        당신은 경제 뉴스 에디터입니다.
        아래 기사를 요약하세요.

        기사:
        %s
        """.textBlock().format(article)

    // ── V3. + 형식·제약 (3불릿 · 40자 · 의견 금지) ──────────────
    private fun v3(article: String): String = """
        당신은 경제 뉴스 에디터입니다.
        아래 기사를 핵심 사실 중심으로
        3개의 불릿, 각 40자 이내로 요약하세요.
        의견이나 전망은 포함하지 마세요.

        기사:
        %s
        """.textBlock().format(article)

    // ── V4. + Few-shot (잘된 요약 예시 2개) ─────────────────────
    // format()을 쓰므로 글자 그대로의 %는 %%로 쓴다 (Java formatted()와 같음)
    private fun v4(article: String): String = """
        당신은 경제 뉴스 에디터입니다.
        아래 기사를 핵심 사실 중심으로
        3개의 불릿, 각 40자 이내로 요약하세요.
        의견이나 전망은 포함하지 마세요.

        좋은 요약의 예시입니다.

        [예시 1]
        - 정부, 반도체 특별법 국회 통과
        - 설비투자 세액공제 15%%로 상향
        - 시행일은 내년 1월 1일

        [예시 2]
        - 원/달러 환율 1,380원 마감
        - 전일 대비 8원 하락
        - 외국인 순매수 4거래일 연속

        이제 아래 기사를 같은 형식으로 요약하세요.

        기사:
        %s
        """.textBlock().format(article)
}
