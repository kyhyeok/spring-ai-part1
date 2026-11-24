package com.example.rag.eval

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/** 평가 세트 모델 (evaluation-set.json 매핑) */
@JsonIgnoreProperties(ignoreUnknown = true)
data class EvaluationSet(val description: String?, val document: String?, val questions: List<EvalQuestion>) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class EvalQuestion(
        val id: String,
        val type: String,               // general | boundary | trap
        val question: String,
        val expectedSource: String?,    // 기대 근거 (trap 은 null)
        val answerable: Boolean,        // false 면 "확인할 수 없습니다"가 정답
    )
}
