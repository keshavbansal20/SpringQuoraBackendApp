package com.example.demo.Services;

import com.example.demo.dto.PagedResponseDTO;
import com.example.demo.dto.QuestionRequestDTO;
import com.example.demo.dto.QuestionResponseDTO;
import com.example.demo.models.QuestionElasticDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IQuestionService {
    public Mono<QuestionResponseDTO> createQuestion(QuestionRequestDTO questionRequestDTO);

    Mono<PagedResponseDTO<QuestionResponseDTO>> getAllQuestions(String cursor, int size, String sortBy, String sortOrder);

    void findAll();

    Flux<QuestionResponseDTO> searchQuestions(String query , int page, int size);

    public Mono<QuestionResponseDTO> getQuestionById(String questionId);

    public List<QuestionElasticDocument> searchQuestionByElasticsearch(String query);
}
