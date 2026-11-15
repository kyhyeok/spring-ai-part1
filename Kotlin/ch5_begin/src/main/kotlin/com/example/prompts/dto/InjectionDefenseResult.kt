package com.example.prompts.dto

/**
 * 인젝션 공격과 3중 방어(Defense in Depth) 결과.
 * 각 방어층이 어디서 걸러냈는지를 그대로 노출해, "한 겹으로는 부족하다"를 눈으로 확인
 */
data class InjectionDefenseResult(
    val userInput: String,

    // 방어 없는 봇
    val undefendedAnswer: String?,

    // 1차 — 입력 검증 (요청이 모델에 닿기 전)
    val inputBlocked: Boolean,
    val matchedInputPatterns: List<String>,

    // 2차 — 시스템 프롬프트 방어 (모델 스스로 버티게). 1차에서 막히면 모델을 부르지 않으므로 null
    val hardenedAnswer: String?,

    // 3차 — 출력 검증 (새어 나가기 전 마지막 문)
    val outputBlocked: Boolean,
    val outputViolations: List<String>,

    val finalAnswer: String?,
)
