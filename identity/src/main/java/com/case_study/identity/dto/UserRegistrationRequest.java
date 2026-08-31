package com.case_study.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload required to register new user")
public class UserRegistrationRequest {

    @NotBlank(message = "name must not be blank")
    @Size(max = 150, message = "name must be at most 150 characters")
    @Schema(description = "Full name of the user", example = "Jomanah Al-Faris")
    private String name;

    @NotBlank(message = "email must not be blank")
    @Email(message = "email must be a valid email address")
    @Size(max = 255, message = "email must be at most 255 characters")
    @Schema(description = "Email address of the user", example = "Jomanahmf@gmail.com")
    private String email;
}
