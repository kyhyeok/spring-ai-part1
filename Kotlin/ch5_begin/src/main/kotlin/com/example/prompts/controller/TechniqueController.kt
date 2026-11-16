package com.example.prompts.controller

import com.example.prompts.dto.BeforeAfterResponse
import com.example.prompts.service.ReasoningService
import com.example.prompts.service.SentimentClassifyService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Zero-shot vs Few-shot · CoT · 기법 선택 치트시트
 *
 * 응답 Map은 mapOf로 만들어 JSON 키가 선언 순서 그대로 나간다 (Java Map.of는 실행마다 순서가 다를 수 있음, 내용은 같음).
 */
@RestController
@RequestMapping("/api/s05/technique")
class TechniqueController(
    private val sentimentService: SentimentClassifyService,
    private val reasoningService: ReasoningService,
) {

    /** Zero-shot — 장황한 응답이 나오는 것을 먼저 확인 */
    @GetMapping("/zero-shot")
    fun zeroShot(
        @RequestParam(defaultValue = "가격 대비 성능은 괜찮은데 배터리가 하루도 못 갑니다") review: String,
    ): Map<String, String> = mapOf(
        "review" to review,
        "answer" to checkAnswer(sentimentService.zeroShot(review)),
    )

    /** Few-shot — 한 단어 출력으로 수렴 */
    @GetMapping("/few-shot")
    fun fewShot(
        @RequestParam(defaultValue = "가격 대비 성능은 괜찮은데 배터리가 하루도 못 갑니다") review: String,
    ): Map<String, String> = mapOf(
        "review" to review,
        "answer" to checkAnswer(sentimentService.fewShot(review)),
    )

    /** 나란히 비교 — "예시가 곧 사양(Spec)" */
    @GetMapping("/shot/compare")
    fun shotCompare(
        @RequestParam(defaultValue = "가격 대비 성능은 괜찮은데 배터리가 하루도 못 갑니다") review: String,
    ): BeforeAfterResponse = sentimentService.compare(review)

    /** CoT 유/무 비교 — 조건 3개 이상이 얽힌 판단 */
    @GetMapping("/cot/compare")
    fun cotCompare(@RequestParam(required = false) problem: String?): BeforeAfterResponse? = null

    /** 기법 선택 치트시트 — 증상으로 찾아 쓰는 처방전 (다운로드 자료와 동일 내용) */
    @GetMapping("/cheatsheet")
    fun cheatsheet(): List<Map<String, String>> = CHEATSHEET

    // Java의 Map.of는 null 값을 받지 않아 답변이 null이면 NPE(500)가 난다. 같은 동작을 위해 null이면 예외
    private fun checkAnswer(answer: String?): String = checkNotNull(answer) { "답변이 없습니다" }

    companion object {
        // 내용이 고정이라 한 번만 만든다.
        // mapOf는 넣은 순서를 지키므로 JSON 키가 항상 증상·처방·이유·실습 순서 (Java Map.of는 실행마다 순서가 다를 수 있음)
        private val CHEATSHEET: List<Map<String, String>> = listOf(
            entry(
                "출력이 들쭉날쭉하다", "출력 형식 명시 + Few-shot 예시",
                "형식은 설명보다 예시가 고정", "/api/s05/technique/shot/compare",
            ),
            entry(
                "답변 관점이 어긋난다", "역할(Persona) 부여",
                "전문성 수준과 관점 고정", "/api/s05/role/separated",
            ),
            entry(
                "없는 사실을 지어낸다", "제약 명시 (\"모르면 모른다고\")",
                "환각의 1차 방어선", "/api/s05/system-prompt/out-of-policy",
            ),
            entry(
                "복잡한 판단을 틀린다", "Chain-of-Thought",
                "단계별 검토 후 결론", "/api/s05/technique/cot/compare",
            ),
            entry(
                "도메인 분류가 부정확하다", "Few-shot 2~5개",
                "우리 도메인의 정답 예시 제공", "/api/s05/technique/few-shot",
            ),
            entry(
                "프롬프트가 코드에 흩어져 있다", "Template + 외부 파일 분리",
                "리뷰·버전 관리 대상으로", "/api/s05/template/prompt-file",
            ),
        )

        private fun entry(symptom: String, prescription: String, why: String, tryIt: String): Map<String, String> =
            mapOf("증상" to symptom, "처방" to prescription, "이유" to why, "실습" to tryIt)
    }
}
