package com.Bank;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String Error;
        private String message;
        private Map<String, String> validationErrors;
        private String path;
    }

