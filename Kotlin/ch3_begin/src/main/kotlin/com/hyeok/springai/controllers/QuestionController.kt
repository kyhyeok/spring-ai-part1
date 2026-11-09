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

    @PostMapping("/ask")
    fun askQuestion(@RequestBody question: Question): Answer =
        openAIService.getAnswer(question)
}
