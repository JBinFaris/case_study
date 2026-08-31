package com.case_study.identity.repository.model;

public interface UserRepository {

    User save(User user);


    User findByEmail(String email);
}
