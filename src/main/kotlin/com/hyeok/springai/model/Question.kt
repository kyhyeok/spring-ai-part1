package com.hyeok.springai.model

// Java record → Kotlin data class
// question을 non-null(String)로 선언 — Java record와 달리 {"question": null}이나 {}가 오면
// jackson-module-kotlin이 역직렬화 단계에서 거부해 400 Bad Request가 된다 (Java는 null로 통과)
data class Question(val question: String)
