package com.example.demo.repositories;

import com.example.demo.models.Question;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface    QuestionRepository extends ReactiveMongoRepository<Question, String> {

    Flux<Question> findTop10ByOrderByCreatedAtAsc();
}
