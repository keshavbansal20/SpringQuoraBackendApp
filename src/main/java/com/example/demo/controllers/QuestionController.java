package com.example.demo.controllers;

import com.example.demo.dto.QuestionRequestDTO;
import com.example.demo.dto.QuestionResponseDTO;
import com.example.demo.models.Question;
import com.example.demo.models.QuestionElasticDocument;
import com.example.demo.services.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public Mono<QuestionResponseDTO> createQuestion(@RequestBody QuestionRequestDTO request) {
        // TEST LOG - If you see this, the controller is receiving the request!
        System.out.println("##################################################");
        System.out.println("### CONTROLLER RECEIVED POST REQUEST ###");
        System.out.println("##################################################");
        System.err.println("ERROR STREAM: Controller received request with title: " + request.getTitle());
        
        System.out.println("=== CONTROLLER: Received request to create question ===");
        System.out.println("Title: " + request.getTitle());
        return questionService.createQuestion(request)
                .doOnNext(response -> System.out.println("=== CONTROLLER: Question created successfully ==="))
                .doOnError(error -> System.err.println("=== CONTROLLER: Error creating question: " + error.getMessage() + " ==="));
    }

    @GetMapping
    public Flux<QuestionResponseDTO> getAllQuestions(@RequestParam(required = false) String cursor,@RequestParam(defaultValue = "10") int size) {
        return questionService.getAllQuestions(cursor,size);
    }

    // Debug endpoint
    @GetMapping("/all")
    public Flux<QuestionResponseDTO> getAllQuestionsNoLimit() {
        return questionService.getAllQuestionsNoLimit();
    }

    @DeleteMapping("/{id}")
    public  Mono<Void> deleteQuestionById(@PathVariable String id){
        throw new UnsupportedOperationException("Not Implemented");
    }

    @GetMapping("/{id}")
    public Mono<QuestionResponseDTO> getQuestionById(@PathVariable String id){
        return questionService.getQuestionById(id)
                .doOnError(error->System.out.println("Error Fetching question: "+error))
                .doOnSuccess(response -> System.out.println("Questions found successfully"+response));
    }
    @GetMapping("/search")
    public Flux<QuestionResponseDTO> searchQuestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return questionService.searchQuestions(query,page,size);
    }

    @GetMapping("/tag/{tag}")
    public Flux<QuestionResponseDTO> getAllQuestionsByTag(@PathVariable String tag
        ,@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size
    )
    {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @GetMapping("/elasticsearch")
    public List<QuestionElasticDocument> searchQuestionByElasticsearch(@RequestParam String query){
        return questionService.searchQuestionByElasticsearch(query);
    }
}
