package com.case_study.identity.exception;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error payload returned by the global exception handler.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private Map<String, String> fieldErrors;
}
