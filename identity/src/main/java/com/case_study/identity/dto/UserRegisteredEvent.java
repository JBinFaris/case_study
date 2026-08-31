package com.case_study.identity.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisteredEvent implements Serializable {


    private Long userId;
    private String name;
    private String email;
    private LocalDateTime registeredAt;


}
