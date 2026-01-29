package com.example.demo.repositories;

import com.example.demo.models.Answer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AnswerRepository extends ReactiveMongoRepository<Answer, String> {

    Flux<Answer> findByQuestionId(String questionId);

    Flux<Answer> findByQuestionIdOrderByCreatedAtDesc(String questionId);
    Flux<Answer> findByQuestionIdOrderByCreatedAtAsc(String questionId);
    Mono<Long> countByQuestionId(String questionId);
}
