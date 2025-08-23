package com.example.demo.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class QuestionRequestDTO {

    @NotBlank(message = "Title is Required")
    @Size(min = 10 , max = 100 , message ="Title is required between 10 and 100")
    private String title;

    @NotBlank(message = "Content is Required")
    @Size(min=10 , max=1000, message = "Content is Required with minimum characters between 10 and 1000")
    private String content;
}
