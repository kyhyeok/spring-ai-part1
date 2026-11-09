package com.hyeok.springai.services;

import com.hyeok.springai.model.Answer;
import com.hyeok.springai.model.Question;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OpenAIServiceImpl implements OpenAIService {

	private final ChatModel chatModel;

	@Value("${spring.ai.openai.chat.model:기본값 사용}")
	private String modelName;

	public OpenAIServiceImpl(ChatModel chatModel) {
		this.chatModel = chatModel;
	}

	@Override
	public Answer getAnswer(Question question) {
		System.out.println("현재 사용하는 ChatModel 구현체: " + chatModel.getClass().getName());
		System.out.println("현재 설정된 OpenAI 모델: " + modelName);

		PromptTemplate promptTemplate = new PromptTemplate(question.question());
		Prompt prompt = promptTemplate.create();

		ChatResponse response = chatModel.call(prompt);

		return new Answer(response.getResult().getOutput().getText());
	}
}
