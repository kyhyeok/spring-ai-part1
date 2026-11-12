package com.hyeok.springai.model

// confidence는 LLM 버전에서 null로 받으므로 nullable로 둔다.
// 그래서 entity()가 만드는 JSON 스키마에서 이 필드만 Java와 다르다
// (Java record: required + number / Kotlin: required 아님 + number 또는 null).
data class ReviewAnalysis(
    val genre: Genre,              // 리뷰로 추정한 장르
    val sentiment: Double,         // 0(매우 부정) ~ 4(매우 긍정)
    val spoiler: Boolean,          // 스포일러 포함 여부
    val confidence: Double?,       // Jev만 제공 (LLM 버전은 null)
)
