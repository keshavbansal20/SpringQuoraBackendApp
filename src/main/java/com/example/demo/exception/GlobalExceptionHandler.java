package com.example.demo.exception;

import com.example.demo.dto.ErrorResponseDTO;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
            WebExchangeBindException ex , ServerWebExchange exchange
    ){

        //error message 
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed");
                        
        ErrorResponseDTO errorResponseDTO = ErrorResponseDTO.builder()
                                            .timestamp(LocalDateTime.now())
                                            .status(HttpStatus.BAD_REQUEST.value())
                                            .error("Validation Failed")
                                            .message(errorMessage)
                                            .path(exchange.getRequest().getPath().value())
                                            .build();
        return new ResponseEntity<>(errorResponseDTO , HttpStatus.BAD_REQUEST);
    }

    // Handle generic exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericExceptions(
        Exception ex , ServerWebExchange exchange
    ){
        ErrorResponseDTO errorResponseDTO = ErrorResponseDTO.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .error("Internal Server Error")
        .message(ex.getMessage())
        .path(exchange.getRequest().getPath().value())
        .build();
        return new ResponseEntity<>(errorResponseDTO , HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
}
