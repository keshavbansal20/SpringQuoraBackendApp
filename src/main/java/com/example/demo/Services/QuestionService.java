package com.example.demo.services;

import com.example.demo.adapters.QuestionAdapter;
import com.example.demo.dto.QuestionRequestDTO;
import com.example.demo.dto.QuestionResponseDTO;
import com.example.demo.models.Question;
import com.example.demo.repositories.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    public Mono<QuestionResponseDTO> createQuestion(QuestionRequestDTO request) {
        Question question = Question.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return questionRepository.save(question).flatMap(savedQuestion-> questionRepository.count()
                .map(count -> QuestionAdapter.toQuestionResponseDTO( savedQuestion ,count)));
    }

    public Flux<QuestionResponseDTO> getAllQuestions(String cursor ,int size) {
        if(cursor!=null && cursor.trim().length()>0){
            Pageable pageable = PageRequest.of(0, size);
            LocalDateTime cursorDateTime = LocalDateTime.parse(cursor);
            return questionRepository.findByCreatedAtGreaterThanOrderByCreatedAtAsc(cursorDateTime,pageable)
                    .index()
                    .map( tuple -> QuestionAdapter.toQuestionResponseDTO(tuple.getT2(),tuple.getT1()))
                    .doOnNext(q -> System.out.println("Fetched question: " + q.getTitle()));
        }else{
            return questionRepository.findTop10ByOrderByCreatedAtAsc()
                    .take(size)
                    .index()
                    .map(tuple -> QuestionAdapter.toQuestionResponseDTO(tuple.getT2(), tuple.getT1()))
                    .doOnNext(q -> System.out.println("Fetched question: " + q.getTitle()));
        }

    }

    // For debugging
    public Flux<QuestionResponseDTO> getAllQuestionsNoLimit() {
        return questionRepository.findAll()
                .index()
                .map(tuple -> {
                    long index = tuple.getT1();
                    Question question = tuple.getT2();
                    return QuestionResponseDTO.builder()
                            .serialNo((int) index + 1)   // serial number starting from 1
                            .id(question.getId())
                            .title(question.getTitle())
                            .content(question.getContent())
                            .createdAt(question.getCreatedAt())
                            .build();
                })
                .doOnNext(q -> System.out.println("Fetched question: " + q.getTitle()));
    }
}
