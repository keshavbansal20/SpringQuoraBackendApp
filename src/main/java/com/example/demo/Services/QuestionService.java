package com.example.demo.services;

import com.example.demo.Services.IQuestionService;
import com.example.demo.adapters.QuestionAdapter;
import com.example.demo.dto.QuestionRequestDTO;
import com.example.demo.dto.QuestionResponseDTO;
import com.example.demo.events.ViewCountEvent;
import com.example.demo.models.Question;
import com.example.demo.producers.KafkaEventProducer;
import com.example.demo.repositories.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuestionService implements IQuestionService {

    private final QuestionRepository questionRepository;

    private final KafkaEventProducer kafkaEventProducer;

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
                    .map( tuple -> QuestionAdapter.toQuestionResponseDTO(tuple.getT2(),(tuple.getT1()+1)))
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

    @Override
    public Flux<QuestionResponseDTO> searchQuestions(String query, int page, int size) {
        return questionRepository.findByTitleOrContentContainingIgnoreCase(query ,PageRequest.of(page ,size))
                .index()
                .map(tuple -> QuestionAdapter.toQuestionResponseDTO(tuple.getT2(),(tuple.getT1()+1)))
                .doOnError(error -> System.out.println("Error searching questions: " + error))
                .doOnComplete(() -> System.out.println("Questions searched successfully"));
    }

    @Override
    public void findAll(){
        return;
    }

    @Override
    public Mono<QuestionResponseDTO> getQuestionById(String questionId) {
        return questionRepository.findById(questionId).
                map(QuestionAdapter::toQuestionResponseDTO)
                .doOnError(error -> System.out.println("Error getting questions: "+error))
                .doOnSuccess(response -> {
                    System.out.println("Questions found successfully"+response);
                    ViewCountEvent viewCountEvent = new ViewCountEvent(questionId,"questions",LocalDateTime.now());
                    kafkaEventProducer.publishViewCountEvent(viewCountEvent);
                });
    }
}
