package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
public class QuestionResponseDTO {
    private Integer serialNo;
    private String id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
}
