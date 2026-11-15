package com.hyeok.springai.model

// confidence는 LLM 버전에서 null로 받으므로 nullable로 둔다.
// 그래서 entity()가 만드는 JSON 스키마에서 이 필드만 Java와 다르다
// (Java record: required + number / Kotlin: required 아님 + number 또는 null).
// @JsonProperty(required = true)를 붙여도 타입은 여전히 number 또는 null이고,
// LLM이 confidence를 빼면 예외가 나므로 쓰지 않는다.
// genre는 non-null이라 스키마는 Java와 같다. 대신 LLM이 스키마를 어기고 genre를 null로 주면
// Java는 null을 담아 200, Kotlin은 역직렬화 예외로 500이다.
data class ReviewAnalysis(
    val genre: Genre,              // 리뷰로 추정한 장르
    val sentiment: Double,         // 0(매우 부정) ~ 4(매우 긍정)
    val spoiler: Boolean,          // 스포일러 포함 여부
    val confidence: Double?,       // Jev만 제공 (LLM 버전은 null)
)
