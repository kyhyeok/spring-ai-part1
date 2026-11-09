package com.hyeok.springai.services;

import com.hyeok.springai.model.Answer;
import com.hyeok.springai.model.Question;

public interface OpenAIService {

	Answer getAnswer(Question question);
}
