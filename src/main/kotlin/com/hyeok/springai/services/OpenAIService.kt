package com.hyeok.springai.services

import com.hyeok.springai.model.Answer
import com.hyeok.springai.model.Question

interface OpenAIService {

    fun getAnswer(question: Question): Answer
}
