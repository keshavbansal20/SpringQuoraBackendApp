package com.example.demo.services;

import com.example.demo.Services.IQuestionService;
import com.example.demo.Services.QuestionIndexService;
import com.example.demo.adapters.QuestionAdapter;
import com.example.demo.dto.PagedResponseDTO;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    
    /**
     * This method creates a Sort object that tells Spring how to sort the results
     * 
     * @param sortBy - which field to sort by (like "createdAt" or "views")
     * @param sortOrder - how to sort: "asc" for ascending, "desc" for descending
     * @return Sort object that Spring can use
     */
    private Sort createSort(String sortBy, String sortOrder) {
        // Step 1: Check if the sortBy field is valid
        // We only allow sorting by createdAt, updatedAt, or views
        // If someone sends an invalid field, we use "createdAt" as default
        String fieldToSortBy = "createdAt"; // default value
        
        if (sortBy.equals("createdAt")) {
            fieldToSortBy = "createdAt";
        } else if (sortBy.equals("updatedAt")) {
            fieldToSortBy = "updatedAt";
        } else if (sortBy.equals("views")) {
            fieldToSortBy = "views";
        }
        // If none of the above, fieldToSortBy stays as "createdAt" (the default)
        
        // Step 2: Figure out the direction (ascending or descending)
        Sort.Direction direction;
        if (sortOrder.equalsIgnoreCase("asc")) {
            direction = Sort.Direction.ASC; // A to Z, 1 to 10, oldest to newest
        } else {
            direction = Sort.Direction.DESC; // Z to A, 10 to 1, newest to oldest
        }
        
        // Step 3: Create and return the Sort object
        return Sort.by(direction, fieldToSortBy);
    }
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


    /**
     * Get all questions with pagination and sorting
     * 
     * @param cursor - optional: if provided, get questions after this date
     * @param size - how many questions to return per page
     * @param sortBy - which field to sort by
     * @param sortOrder - "asc" or "desc"
     * @return PagedResponseDTO containing the questions and pagination info
     */
    public Mono<PagedResponseDTO<QuestionResponseDTO>> getAllQuestions(
            String cursor, int size, String sortBy, String sortOrder) {
        
        // Step 1: Create the sort object using our helper method
        Sort sort = createSort(sortBy, sortOrder);
        
        // Step 2: Create pageable object
        // PageRequest.of(pageNumber, pageSize, sort)
        // We use page 0 (first page) for now
        int pageNumber = 0;
        Pageable pageable = PageRequest.of(pageNumber, size, sort);
        
        // Step 3: Check if user wants cursor-based pagination or regular pagination
        if (cursor != null && !cursor.trim().isEmpty()) {
            // CURSOR-BASED PAGINATION (for infinite scroll)
            // User provided a cursor (date), so get questions after that date
            
            // Convert the cursor string to a date
            LocalDateTime cursorDate = LocalDateTime.parse(cursor);
            
            // Get questions from database that were created after the cursor date
            return questionRepository.findByCreatedAtGreaterThanOrderByCreatedAtAsc(cursorDate, pageable)
                    // Convert Flux<Question> to List<Question>
                    .collectList()
                    // Now we have the questions, but we also need the total count
                    .flatMap(questionsList -> {
                        // Get total count of all questions in database
                        return questionRepository.count()
                                .map(totalCount -> {
                                    // Convert Question objects to QuestionResponseDTO objects
                                    List<QuestionResponseDTO> questionDTOs = new ArrayList<>();
                                    for (Question question : questionsList) {
                                        QuestionResponseDTO dto = QuestionAdapter.toQuestionResponseDTO(question, 0);
                                        questionDTOs.add(dto);
                                    }
                                    
                                    // Calculate total pages
                                    // Example: 25 total questions, 10 per page = 3 pages (round up)
                                    int totalPages = (int) Math.ceil((double) totalCount / size);
                                    
                                    // Build and return the response
                                    return PagedResponseDTO.<QuestionResponseDTO>builder()
                                            .content(questionDTOs)           // The list of questions
                                            .currentPage(pageNumber)          // Current page (0)
                                            .pageSize(size)                   // How many per page
                                            .totalElements(totalCount)        // Total questions in DB
                                            .totalPages(totalPages)           // Total number of pages
                                            .hasNext(totalCount > size)       // Are there more pages?
                                            .hasPrevious(false)               // Is there a previous page? (no, we're on first page)
                                            .build();
                                });
                    });
        } else {
            // REGULAR PAGINATION (no cursor provided)
            // Get all questions, sorted and paginated
            
            // Get questions from database with pagination and sorting
            return questionRepository.findAllBy(pageable)
                    // Convert Flux<Question> to List<Question>
                    .collectList()
                    // Now we have the questions, but we also need the total count
                    .flatMap(questionsList -> {
                        // Get total count of all questions in database
                        return questionRepository.count()
                                .map(totalCount -> {
                                    // Convert Question objects to QuestionResponseDTO objects
                                    List<QuestionResponseDTO> questionDTOs = new ArrayList<>();
                                    for (Question question : questionsList) {
                                        QuestionResponseDTO dto = QuestionAdapter.toQuestionResponseDTO(question, 0);
                                        questionDTOs.add(dto);
                                    }
                                    
                                    // Calculate total pages
                                    int totalPages = (int) Math.ceil((double) totalCount / size);
                                    
                                    // Build and return the response
                                    return PagedResponseDTO.<QuestionResponseDTO>builder()
                                            .content(questionDTOs)           // The list of questions
                                            .currentPage(pageNumber)          // Current page (0)
                                            .pageSize(size)                   // How many per page
                                            .totalElements(totalCount)        // Total questions in DB
                                            .totalPages(totalPages)           // Total number of pages
                                            .hasNext(totalCount > size)       // Are there more pages?
                                            .hasPrevious(false)               // Is there a previous page?
                                            .build();
                                });
                    });
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
