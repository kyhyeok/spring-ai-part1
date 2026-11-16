package com.example.prompts.controller

import com.example.prompts.service.ChatService
import com.example.prompts.service.TravelService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * PromptTemplate 변수 바인딩 · Prompt 외부 파일 관리
 *
 * 응답 Map은 mapOf로 만들어 JSON 키가 선언 순서 그대로 나간다 (Java Map.of는 실행마다 순서가 다를 수 있음, 내용은 같음).
 */
@RestController
@RequestMapping("/api/s05/template")
class PromptTemplateController(
    private val travelService: TravelService,
    private val chatService: ChatService,
) {

    /** 대비용 — 문자열 연결(+) 방식 */
    @GetMapping("/travel/concat")
    fun travelConcat(
        @RequestParam(defaultValue = "여행 전문가") role: String,
        @RequestParam(defaultValue = "번호 목록 3개") format: String,
        @RequestParam(defaultValue = "가을에 가기 좋은 국내 여행지를 추천해 주세요") question: String,
    ): Map<String, String> = mapOf(
        "answer" to checkAnswer(travelService.recommendByConcat(role, format, question)),
    )

    /** 슬라이드 코드 그대로 — 여행 전문가 · 번호 목록 3개 */
    @GetMapping("/travel")
    fun travel(
        @RequestParam(defaultValue = "가을에 가기 좋은 국내 여행지를 추천해 주세요") question: String,
    ): Map<String, String> = mapOf(
        "question" to question,
        "answer" to checkAnswer(travelService.recommend(question)),
    )

    /** 템플릿 재사용 — 역할·형식만 바꿔 다른 도메인에 적용 */
    @GetMapping("/travel/custom")
    fun travelCustom(
        @RequestParam(defaultValue = "요리 연구가") role: String,
        @RequestParam(defaultValue = "표 형식") format: String,
        @RequestParam(defaultValue = "1인 가구를 위한 간단한 저녁 메뉴를 알려주세요") question: String,
    ): Map<String, String> = mapOf(
        "answer" to checkAnswer(travelService.recommendCustom(role, format, question)),
    )

    /** 현재 로딩된 system.st 원문 확인 — "프롬프트도 리뷰 대상" */
    @GetMapping("/prompt-file")
    fun promptFile(): Map<String, String> = mapOf(
        "classpath:/prompts/system.st" to chatService.currentSystemPrompt(),
    )

    /** 슬라이드 코드 그대로 — Resource를 system()에 직접 전달 ({service} 미치환) */
    @GetMapping("/chat/raw")
    fun chatRaw(
        @RequestParam(defaultValue = "환불은 며칠 이내에 신청해야 하나요?") question: String,
    ): Map<String, String> = mapOf(
        "answer" to checkAnswer(chatService.ask(question)),
        "확인 포인트" to "{service} 자리가 치환되지 않은 채 전달 됩니다.",
    )

    /** 실전형 — {service} 바인딩 후 전달 */
    @GetMapping("/chat")
    fun chat(
        @RequestParam(defaultValue = "OO쇼핑 고객센터") service: String,
        @RequestParam(defaultValue = "환불은 며칠 이내에 신청해야 하나요?") question: String,
    ): Map<String, String> = mapOf(
        "service" to service,
        "answer" to checkAnswer(chatService.ask(service, question)),
    )

    // Java의 Map.of는 null 값을 받지 않아 답변이 null이면 NPE(500)가 난다. 같은 동작을 위해 null이면 예외
    private fun checkAnswer(answer: String?): String = checkNotNull(answer) { "답변이 없습니다" }
}
