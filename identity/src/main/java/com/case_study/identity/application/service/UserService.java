package com.case_study.identity.application.service;

import com.case_study.identity.application.dto.UserRegistrationRequest;
import com.case_study.identity.application.dto.UserRegistrationResponse;
import com.case_study.identity.infra.model.UserRepository;
import io.netty.util.internal.ObjectUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Transactional
    public UserRegistrationResponse register(UserRegistrationRequest request){
        if (ObjectUtils.isEmpty(userRepository.findByEmail(request.getEmail()))) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

    }

}
