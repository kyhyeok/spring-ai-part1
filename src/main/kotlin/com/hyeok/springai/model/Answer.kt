package com.hyeok.springai.model

// Java record → Kotlin data class
// answer는 String? — Spring AI의 AssistantMessage.getText()가 @Nullable이라
// 모델 응답에 텍스트가 없으면 null이 될 수 있다 (Java record와 동일하게 {"answer": null}로 직렬화)
data class Answer(val answer: String?)
