package com.Bank;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
/**
 * Global exception handler for the REST API.
 * <p>
 * Catches all exceptions thrown in controllers and services,
 * and converts them into a uniform {@link ErrorResponse} format.
 * </p>
 *
 * <p><b>Handled exception types:</b></p>
 * <ul>
 * <li>{@link ResourceNotFoundException} → 404 Not Found</li>
 * <li>{@link InsufficientFundsException} → 400 Bad Request</li>
 * <li>{@link MethodArgumentNotValidException} → 400 Bad Request (with validation details)</li>
 * <li>{@link AccessDeniedException} → 403 Forbidden</li>
 * <li>{@link Exception} (all others) → 500 Internal Server Error</li>
 * </ul>
 *
 * <p><b>Example response:</b></p>
 * <pre>
 * {
 * "timestamp": "2025-01-15T10:30:00",
 * "status": 404,
 * "error": "Not Found",
 * "message": "Account number 1234567890 not found",
 * "path": "/api/accounts/1234567890"
 * }
 * </pre>
 *
 * @author Valep Vinreo
 * @version 1.0
 * @see ErrorResponse
 * @since 04-2026
 */
@Slf4j
@RestControllerAdvice
public class Global_Exception_Handler {
    /**
     * Handles the {@link ResourceNotFoundException} exception.
     * <p>
     * Returns HTTP status 404 (Not Found) with information about the resource not found.
     * </p>
     *
     * @param ex exception
     * @param request current HTTP request
     * @return response with code 404
     */
        //Not implemented yet. Saved for future
        @ExceptionHandler(ResourceNotFoundException.class)
        private ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, WebRequest request){
            log.error("Resource not found", ex.getMessage());
            ErrorResponse response = ErrorResponse.builder().timestamp(LocalDateTime.now()).status(HttpStatus.NOT_FOUND.value())
                    .Error("Not found").message(ex.getMessage()).path(getPath(request)).build();
            return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
        }
    /**
     * Handles the {@link InsufficientFundsException} exception.
     * <p>
     * Returns HTTP status 400 (Bad Request) with details about the insufficient funds.
     * </p>
     *
     * @param ex exception
     * @param request current HTTP request
     * @return response with code 400
     */
        //Not implemented yet. Saved for future
        @ExceptionHandler(InsufficientFundsException.class)
        private ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex, WebRequest request){
            log.error("not enough funds: {}",ex.getMessage());
            ErrorResponse response = ErrorResponse.builder().timestamp(LocalDateTime.now()).status(HttpStatus.BAD_REQUEST.value())
                    .Error("Bad Request").message(ex.getMessage()).path(getPath(request)).build();
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    /**
     * Handles validation errors (@Valid).
     * <p>
     * Returns HTTP status 400 with a detailed list of errors for each field.
     * </p>
     *
     * @param ex validation exception
     * @param request current HTTP request
     * @return response with code 400 and error list
     */
    //Not implemented yet. Saved for future
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation error: {}", errors);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .Error("Validation Failed")
                .message("Input validation error")
                .validationErrors(errors)
                .path(getPath(request))
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    /**
     * Handles the {@link AccessDeniedException} exception.
     * <p>
     * Returns HTTP status 403 (Forbidden) when the user
     * does not have sufficient permissions to access the resource.
     * </p>
     *
     * @param ex exception
     * @param request current HTTP request
     * @return response with code 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .Error("Forbidden")
                .message("You don't have access to this recourse")
                .path(getPath(request))
                .build();

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
    /**
     * Handles all other exceptions (unexpected errors).
     * <p>
     * Returns HTTP status 500 (Internal Server Error).
     * Important: Do not disclose error details to the client in production!
     * </p>
     *
     * @param ex exception
     * @param request current HTTP request
     * @return response with code 500
     */
        //Not implemented yet. Saved for future
        @ExceptionHandler(Exception.class)
        private ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request){
            log.error("Internal server error", ex);
            ErrorResponse response = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .Error("Internal server error")
                    .message("Internal server error has occurred")
                    .path(getPath(request)).build();
            return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    /**
     * Extracts the path (URI) from the request.
     *
     * @param request - the current HTTP request
     * @return the path where the error occurred
     */
    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
