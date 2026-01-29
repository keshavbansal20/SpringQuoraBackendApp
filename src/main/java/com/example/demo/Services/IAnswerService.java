package com.example.demo.Services;

import com.example.demo.dto.AnswerRequestDTO;
import com.example.demo.dto.AnswerResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IAnswerService {

    public Mono<AnswerResponseDTO> createAnswer(AnswerRequestDTO answerRequestDTO);

    public Mono<AnswerResponseDTO> getAnswerById(String id);

    // update answer
    public Mono<AnswerResponseDTO> updateAnswer(String id,AnswerRequestDTO answerRequestDTO);

    //delete answer
    public Mono<Void> deleteAnswer(String id);

    //getallanswers()
    public Flux<AnswerResponseDTO> getAllAnswers();

    //get answer on question
    public Flux<AnswerResponseDTO> getAllAnswersByQuestionId(String questoinId);

    //get answer count by question id
    public Mono<Long> getAnswerCountByQuestionId(String questionId);

    //in desc
    public Flux<AnswerResponseDTO> getAnswersByQuestionIdOrderByCreatedAtDesc(String questionId);

    //in inc
    public Flux<AnswerResponseDTO> getAnswersByQuestionIdOrderByCreatedAtAsc(String questionId);




}
