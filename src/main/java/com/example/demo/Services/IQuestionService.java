package com.example.demo.Services;

import com.example.demo.dto.QuestionRequestDTO;
import com.example.demo.dto.QuestionResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IQuestionService {
    public Mono<QuestionResponseDTO> createQuestion(QuestionRequestDTO questionRequestDTO);

    Flux<QuestionResponseDTO> getAllQuestions(String cursor , int size);

    void findAll();

    Flux<QuestionResponseDTO> searchQuestions(String query , int page, int size);

    public Mono<QuestionResponseDTO> getQuestionById(String questionId);
}
