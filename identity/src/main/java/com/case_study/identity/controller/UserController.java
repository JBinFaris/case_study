package com.case_study.identity.controller;


import com.case_study.identity.application.dto.UserRegistrationRequest;
import com.case_study.identity.application.dto.UserRegistrationResponse;
import com.case_study.identity.application.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserService userService;


    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponse> register(
            @Valid @RequestBody UserRegistrationRequest request) {
        UserRegistrationResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



}
