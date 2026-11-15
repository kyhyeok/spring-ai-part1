package com.example.prompts.controller

import com.example.prompts.dto.BeforeAfterResponse
import com.example.prompts.service.PromptQualityService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/** 프롬프트 품질 · 역할 구조 · System Prompt 4요소 */
@RestController
@RequestMapping("/api/s05")
class PromptQualityController(
    private val service: PromptQualityService,
) {

    /** Before/After: "이 기사 요약해줘" vs 역할·형식·제약 명시 */
    @GetMapping("/quality/summary")
    fun summary(@RequestParam(required = false) article: String?): BeforeAfterResponse =
        service.summaryBeforeAfter(article ?: PromptQualityService.SAMPLE_ARTICLE)

    /** 역할 분리(system/user)로 호출 */
    // Java의 Map.of는 null 값을 받지 않아 답변이 null이면 NPE(500)가 난다. 같은 동작을 위해 null이면 예외
    @GetMapping("/role/separated")
    fun roleSeparated(
        @RequestParam(defaultValue = "@Transactional의 전파 속성을 설명해 주세요") question: String,
    ): Map<String, String> = mapOf(
        "question" to question,
        "answer" to checkNotNull(service.roleSeparated(question)) { "답변이 없습니다" },
    )

    /** 안티패턴 비교: 사용자 입력을 system에 섞는 경우 */
    @GetMapping("/role/anti-pattern")
    fun antiPattern(
        @RequestParam(defaultValue = "@Transactional의 전파 속성을 설명해 주세요") question: String,
    ): BeforeAfterResponse = service.roleSeparationAntiPattern(question)

    /** 4요소(역할·형식·제약·톤) 고객센터 봇 */
    @GetMapping("/system-prompt/customer-center")
    fun customerCenter(
        @RequestParam(defaultValue = "OO쇼핑") company: String,
        @RequestParam(defaultValue = "주문한 상품을 개봉했는데 환불이 되나요?") question: String,
    ): Map<String, String>? = null

    /** 제약(하지 말 것)의 효과: 정책에 없는 질문을 넣어 본다 */
    @GetMapping("/system-prompt/out-of-policy")
    fun outOfPolicy(
        @RequestParam(defaultValue = "OO쇼핑") company: String,
        @RequestParam(defaultValue = "이 회사 주가 전망이 어떤가요?") question: String,
    ): Map<String, String>? = null
}
