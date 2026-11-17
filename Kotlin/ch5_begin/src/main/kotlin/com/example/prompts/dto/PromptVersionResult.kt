package com.example.prompts.dto

/**
 * 실습 — "같은 과제, 4개의 Prompt".
 * V1(기본) → V2(+역할) → V3(+형식·제약) → V4(+Few-shot)를 각 3회 실행한 결과.
 * 수강생은 이 결과로 ① 형식 일관성 ② 핵심 포착 ③ 분량 준수 비교표를 작성한다.
 */
data class PromptVersionResult(
    val article: String,
    val runsPerVersion: Int,
    val versions: List<Version>,
) {
    data class Version(
        val id: String,             // V1 ~ V4
        val label: String,          // 기본 / +역할 / +형식·제약 / +Few-shot
        val prompt: String,         // 실제 전송된 프롬프트 원문
        val runs: List<String>,     // 각 회차 응답 (응답이 null이면 Java처럼 500이라 여기엔 null이 없음)
        val lengths: List<Int>,     // 회차별 글자 수 — 분량 준수 판단에 사용
    )
}
