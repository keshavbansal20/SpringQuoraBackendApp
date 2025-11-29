package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LikeRequestDTO {

    @NotBlank(message = "Target ID is required")
    private String targetId;

    @NotBlank(message = "Target Type is required")
    private String targetType;

    @NotNull(message = "isLike is Required")
    private Boolean isLike;
}
