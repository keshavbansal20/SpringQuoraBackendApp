package com.example.demo.models;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collation = "questions")
public class Question {

    @Id
    private String Id;

    @NotBlank(message = "Title is required")
    @Size(min = 10, max=100,message = "Title content must be between 10 and 100 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min=10 , max=1000 , message = "Content must be between 10 and 1000 characters")
    private String content;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
