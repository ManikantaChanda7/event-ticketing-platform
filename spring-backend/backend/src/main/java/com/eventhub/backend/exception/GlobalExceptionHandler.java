package com.eventhub.backend.exception;

import com.eventhub.backend.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
                        MethodArgumentNotValidException ex) {

                Map<String, String> errors = new HashMap<>();

                ex.getBindingResult()
                                .getFieldErrors()
                                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

                ApiResponse<Map<String, String>> response = new ApiResponse<>(
                                false,
                                "Validation failed",
                                errors);

                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
                        ResourceNotFoundException ex) {

                ApiResponse<Void> response = new ApiResponse<>(
                                false,
                                ex.getMessage(),
                                null);

                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        // @ExceptionHandler(Exception.class)
        // public ResponseEntity<ApiResponse<Void>> handleGenericException(
        // Exception ex) {

        // ApiResponse<Void> response = new ApiResponse<>(
        // false,
        // "An error occurred: " + ex.getMessage(),
        // null);

        // return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        // }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleGenericException(
                        Exception ex) {

                ex.printStackTrace();

                Throwable root = ex;
                while (root.getCause() != null) {
                        root = root.getCause();
                }

                System.err.println("ROOT CAUSE:");
                root.printStackTrace();

                ApiResponse<Void> response = new ApiResponse<>(
                                false,
                                "An error occurred: " + root.getMessage(),
                                null);

                return new ResponseEntity<>(
                                response,
                                HttpStatus.INTERNAL_SERVER_ERROR);
        }
}