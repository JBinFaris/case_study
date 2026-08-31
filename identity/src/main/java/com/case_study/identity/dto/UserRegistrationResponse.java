package com.case_study.identity.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Result of a successful user registration")
public class UserRegistrationResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Jomanah Al-Faris")
    private String name;

    @Schema(example = "Jomanahmf@gmail.com")
    private String email;

    @Schema(example = "2026-08-31T10:15:30")
    private LocalDateTime createdAt;

    @Schema(example = "REGISTERED - email notification queued")
    private String status;


}
