package com.Bank;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@RestControllerAdvice
public class Global_Exception_Handler {
        @ExceptionHandler(ResourceNotFoundException.class)
        private ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex){
            log.error("resource not found", ex.getMessage());
            ErrorResponse response = ErrorResponse.builder().timestamp(LocalDateTime.now()).status(HttpStatus.NOT_FOUND.value())
                    .Error("Not found").message(ex.getMessage()).path(getPath()).build();
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        }
        @ExceptionHandler(InsufficientFundsException.class)
        private ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex){
            log.error("not enough funds: {}",ex.getMessage());
            ErrorResponse response = ErrorResponse.builder().timestamp(LocalDateTime.now()).status(HttpStatus.BAD_REQUEST.value())
                    .Error("Bad Request").message(ex.getMessage()).path(getPath()).build();
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
        @ExceptionHandler(MethodArgumentNotValidException.class)
        private ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex){
            Map<String, String> errors = new HashMap<>();
            ex.getBindingResult().getAllErrors().forEach((error) -> {
                String FieldName = ((FieldError) errors).getField();
                String error_Message = error.getDefaultMessage();
                errors.put(FieldName,error_Message);
            });
            ErrorResponse response = ErrorResponse.builder().timestamp(LocalDateTime.now()).status(HttpStatus.BAD_REQUEST.value())
                    .Error("Validation Failed").message("Validation error").validationErrors(errors).path(getPath()).build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(AccessDeniedException.class)
        private ResponseEntity<ErrorResponse> handleAccesDenied(AccessDeniedException ex){
            ErrorResponse response = ErrorResponse.builder().timestamp(LocalDateTime.now()).status(HttpStatus.FORBIDDEN.value())
                    .Error("Forbidden").message("Access to this recource are denied").path(getPath()).build();
            return new ResponseEntity<>(response,HttpStatus.FORBIDDEN);
        }
        @ExceptionHandler(Exception.class)
        private ResponseEntity<ErrorResponse> handleGenericException(Exception ex){
            log.error("Internal server error", ex);
            ErrorResponse response = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .Error("Internal server error")
                    .message("Internal server error has occured")
                    .path(getPath()).build();
            return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        public String getPath(){
            //do logic later
            return "Unknown";
        }
    }
