package com.case_study.identity.service;

import com.case_study.identity.dto.UserRegisteredEvent;
import com.case_study.identity.dto.UserRegistrationRequest;
import com.case_study.identity.dto.UserRegistrationResponse;
import com.case_study.identity.exception.EmailAlreadyExistsException;
import com.case_study.identity.repository.model.User;
import com.case_study.identity.repository.model.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;


import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEventProducer userEventProducer;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Captor
    private ArgumentCaptor<UserRegisteredEvent> eventCaptor;


    private UserRegistrationRequest request;

    @BeforeEach
    void setUp() {
        request = new UserRegistrationRequest("Jomanah Al-Faris", "jomanahmf@gmail.com");
    }

    //we follow  triple A method ( Arrange act assert)
    @Test
    void register_success_savesAndPublishesNotification(){
    // Arrange
        when(userRepository.findByEmail(request.getEmail())).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        //Act
        UserRegistrationResponse response = userService.register(request);

        //Assert
        verify(userRepository, times(1)).save(userCaptor.capture());
        verify(userEventProducer, times(1)).publish(eventCaptor.capture());
        assertThat(response.getStatus()).contains("REGISTERED - notification queued");
    }

    @Test
    void register_fail_EmailAlreadyLinkedWithAccount_throwsError() {
        // Arrange
        User existingUser = getMockedRegisteredUser();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(existingUser);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);
        
        verify(userRepository, never()).save(any(User.class));
        verify(userEventProducer, never()).publish(any(UserRegisteredEvent.class));



    }
        User getMockedRegisteredUser(){
        return new User(1L , "Jomanah" , "Jomanahmf@gmail.com" , LocalDateTime.now());
    }
}
