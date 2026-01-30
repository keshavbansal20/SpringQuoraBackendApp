package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class QuestionRequestDTO {

    @NotBlank(message ="Title is required")
    @Size(min = 10 , max = 100 , message= "Title must be between 10 and 100 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 10  , max = 1000 , message= "Content must be string between 10 and 100 characrers")
    private String content;
}
