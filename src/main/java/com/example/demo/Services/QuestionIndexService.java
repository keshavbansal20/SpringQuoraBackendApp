package com.example.demo.Services;

import com.example.demo.models.Question;
import com.example.demo.models.QuestionElasticDocument;
import com.example.demo.repositories.QuestionDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionIndexService implements IQuestionIndexService {

    private final QuestionDocumentRepository questionDocumentRepository;

    @Override
    public void createQuestionIndex(Question question) {
        System.out.println("=== Starting to index question: " + question.getId() + " ===");
        try {
            QuestionElasticDocument document =  QuestionElasticDocument.builder()
                    .id(question.getId())
                    .title(question.getTitle())
                    .content(question.getContent())
                    .build();

            System.out.println("Attempting to save document to Elasticsearch...");
            QuestionElasticDocument saved = questionDocumentRepository.save(document);
            System.out.println("✅ Successfully indexed question to Elasticsearch: " + question.getId());
            System.out.println("Saved document ID: " + (saved != null ? saved.getId() : "null"));
        } catch (Exception e) {
            System.err.println("❌ Error indexing question to Elasticsearch: " + e.getMessage());
            System.err.println("Exception type: " + e.getClass().getName());
            e.printStackTrace();
        }
        System.out.println("=== Finished indexing attempt for question: " + question.getId() + " ===");
    }
}
