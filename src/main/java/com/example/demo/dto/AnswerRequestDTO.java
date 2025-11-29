package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AnswerRequestDTO {

    @NotBlank(message = "Content is Required")
    @Size(min=10 , max=1000 , message = "Content must be between 10 and 1000 characters")
    private String content;

    @NotBlank(message = "Question Id is required")
    private String questionId;
}
