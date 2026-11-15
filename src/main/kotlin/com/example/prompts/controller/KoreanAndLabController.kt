package com.example.prompts.controller

import com.example.prompts.dto.BeforeAfterResponse
import com.example.prompts.dto.PromptVersionResult
import com.example.prompts.service.KoreanPromptService
import com.example.prompts.service.PromptVersionLabService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/** 한국어 Prompt 전략 · 실습(V1~V4) */
@RestController
@RequestMapping("/api/s05")
class KoreanAndLabController(
    private val koreanService: KoreanPromptService,
    private val labService: PromptVersionLabService,
) {

    /** 언어 조합 A/B — "정답은 실험" */
    @GetMapping("/korean/ab-test")
    fun abTest(
        @RequestParam(defaultValue = "MSA 환경에서 분산 트랜잭션을 다루는 방법") task: String,
    ): BeforeAfterResponse? = null

    /** 시스템 프롬프트 다이어트 — 길이 = 고정비 */
    @GetMapping("/korean/system-diet")
    fun systemDiet(
        @RequestParam(defaultValue = "배송이 지연되면 보상이 있나요?") question: String,
    ): BeforeAfterResponse? = null

    /** 출력 언어 명시 유무 — "운에 맡기지 말 것" */
    @GetMapping("/korean/output-language")
    fun outputLanguage(
        @RequestParam(defaultValue = "Explain the CAP theorem briefly.") question: String,
    ): Map<String, String?>? = null

    /**
     * 실습 — 같은 과제, 4개의 Prompt를 각 3회 실행.
     * 기본 4버전 × 3회 = 12회 호출. 토큰 사용량에 유의.
     */
    @GetMapping("/lab/prompt-versions")
    fun promptVersions(
        @RequestParam(required = false) article: String?,
        @RequestParam(defaultValue = "3") runs: Int,
    ): PromptVersionResult? = null
}
