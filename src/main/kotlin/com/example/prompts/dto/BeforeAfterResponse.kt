package com.example.prompts.dto

/**
 * "Before vs After"를 한 번의 호출로 나란히 보여주기 위한 응답.
 * 프롬프트 원문까지 함께 반환하므로, 응답 화면만으로 "무엇을 추가했더니 어떻게 달라졌는지"가 확인 가능
 *
 * 답변은 Spring AI가 null을 돌려줄 수 있어(@Nullable) String?로 둔다.
 */
data class BeforeAfterResponse(
    val topic: String,
    val beforeLabel: String,
    val beforePrompt: String,
    val beforeAnswer: String?,
    val beforeTokens: Long,
    val afterLabel: String,
    val afterPrompt: String,
    val afterAnswer: String?,
    val afterTokens: Long,
)
