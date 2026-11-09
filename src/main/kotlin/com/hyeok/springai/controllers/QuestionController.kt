package com.hyeok.springai.controllers

import com.hyeok.springai.model.Answer
import com.hyeok.springai.model.Question
import com.hyeok.springai.services.OpenAIService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class QuestionController(
    private val openAIService: OpenAIService,
) {

    // Java 원본(ch3_begin)과 동일하게 아직 미구현 상태 — 이후 강의 실습에서 구현
    @PostMapping("/ask")
    fun askQuestion(@RequestBody question: Question): Answer? = null
}
