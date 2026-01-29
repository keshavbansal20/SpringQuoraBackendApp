package com.example.demo.services;

import com.example.demo.Services.IQuestionService;
import com.example.demo.Services.QuestionIndexService;
import com.example.demo.adapters.QuestionAdapter;
import com.example.demo.dto.QuestionRequestDTO;
import com.example.demo.dto.QuestionResponseDTO;
import com.example.demo.events.ViewCountEvent;
import com.example.demo.models.Question;
import com.example.demo.models.QuestionElasticDocument;
import com.example.demo.producers.KafkaEventProducer;
import com.example.demo.repositories.QuestionDocumentRepository;
import com.example.demo.repositories.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class QuestionService implements IQuestionService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);

    private final QuestionRepository questionRepository;

    private final KafkaEventProducer kafkaEventProducer;
    private final QuestionIndexService questionIndexService;
    private final QuestionDocumentRepository questionDocumentRepository;

    public Mono<QuestionResponseDTO> createQuestion(QuestionRequestDTO request) {
        // DEBUG: Write to file to confirm method is called
        try {
            PrintWriter writer = new PrintWriter(new FileWriter("/tmp/question_debug.log", true));
            writer.println("[" + LocalDateTime.now() + "] createQuestion CALLED - Title: " + request.getTitle());
            writer.close();
        } catch (IOException e) {
            // Ignore
        }
        
        // Use proper logger
        logger.info("========================================");
        logger.info("createQuestion METHOD CALLED - CONFIRMED");
        logger.info("Title: {}", request.getTitle());
        logger.info("Content: {}", request.getContent());
        logger.info("========================================");
        
        System.out.println("=== SERVICE: Starting createQuestion ===");
        Question question = Question.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        System.out.println("=== SERVICE: Question object built, saving to MongoDB ===");

        return questionRepository.save(question)
                .doOnNext(savedQuestion -> System.out.println("=== SERVICE: Question saved to MongoDB with ID: " + savedQuestion.getId() + " ==="))
                .flatMap(savedQuestion-> {
                    System.out.println("=== SERVICE: Inside flatMap, calling indexing service ===");
                    try {
                        questionIndexService.createQuestionIndex(savedQuestion); //dumping the question to elastic search
                        System.out.println("=== SERVICE: Indexing service call completed ===");
                    } catch (Exception e) {
                        System.err.println("=== SERVICE: Failed to index question: " + e.getMessage() + " ===");
                        e.printStackTrace();
                    }
                    return questionRepository.count()
                            .map(count -> QuestionAdapter.toQuestionResponseDTO(savedQuestion, count));
                })
                .doOnError(error -> System.err.println("=== SERVICE: Error in createQuestion: " + error.getMessage() + " ==="));
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

    @Override
    public List<QuestionElasticDocument> searchQuestionByElasticsearch(String query) {
        return questionDocumentRepository.findByTitleContainingOrContentContaining(query , query);

    }

}
