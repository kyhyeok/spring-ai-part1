package com.hyeok.springai.model

// entity()가 프롬프트에 붙이는 JSON 스키마는 Kotlin에서 null 여부로 required를 정한다.
// 모든 필드를 non-null로 두어야 Java record와 같은 스키마(모든 필드 required)가 된다.
// 대신 LLM이 스키마를 어기고 title/director를 null로 주거나 빼면
// Java는 null을 담아 200, Kotlin은 역직렬화 예외로 500이 된다.
data class Movie(
    val title: String,
    val year: Int,
    val director: String,
)
