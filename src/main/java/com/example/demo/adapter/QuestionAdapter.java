package com.example.demo.adapters;

import com.example.demo.dto.QuestionResponseDTO;
import com.example.demo.models.Question;

public class QuestionAdapter {
    // Normal mapping without serial number
    public static QuestionResponseDTO toQuestionResponseDTO(Question question) {
        return QuestionResponseDTO.builder()
                .id(question.getId())
                .title(question.getTitle())
                .content(question.getContent())
                .createdAt(question.getCreatedAt())
                .build();
    }
    public static QuestionResponseDTO toQuestionResponseDTO(Question question,long serialNo) {
        return QuestionResponseDTO.builder()
                .serialNo((int) serialNo)
                .id(question.getId())
                .title(question.getTitle())
                .content(question.getContent())
                .createdAt(question.getCreatedAt())
                .build();
    }
}
