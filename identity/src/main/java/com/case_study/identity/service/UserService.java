package com.case_study.identity.service;

import com.case_study.identity.dto.UserRegisteredEvent;
import com.case_study.identity.dto.UserRegistrationRequest;
import com.case_study.identity.dto.UserRegistrationResponse;
import com.case_study.identity.exception.EmailAlreadyExistsException;
import com.case_study.identity.repository.model.User;
import com.case_study.identity.repository.model.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;

@Service
@Slf4j
public class UserService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserEventProducer userEventProducer;

    @Transactional
    public UserRegistrationResponse register(UserRegistrationRequest request){
        if (!ObjectUtils.isEmpty(userRepository.findByEmail(request.getEmail()))) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .createdAt(LocalDateTime.now())
                .build();

        User registredUser = userRepository.save(user);
        log.info("Persisted new user id={} email={}", registredUser.getId(), registredUser.getEmail());


        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(registredUser.getId())
                .name(registredUser.getName())
                .email(registredUser.getEmail())
                .registeredAt(registredUser.getCreatedAt())
                .build();
        userEventProducer.publish(event);

        return UserRegistrationResponse.builder()
                .id(registredUser.getId())
                .name(registredUser.getName())
                .email(registredUser.getEmail())
                .createdAt(registredUser.getCreatedAt())
                .status("REGISTERED - notification queued")
                .build();



    }

}
