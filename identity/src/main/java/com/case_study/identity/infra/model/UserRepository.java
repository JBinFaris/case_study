package com.case_study.identity.infra.model;

public interface UserRepository {

    User save(User user);


    User findByEmail(String email);
}
