package com.example.demo.controllers;

import com.example.demo.dto.QuestionRequestDTO;
import com.example.demo.dto.QuestionResponseDTO;
import com.example.demo.models.Question;
import com.example.demo.services.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public Mono<QuestionResponseDTO> createQuestion(@RequestBody QuestionRequestDTO request) {
        return questionService.createQuestion(request);
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
}
