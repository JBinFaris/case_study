package com.case_study.identity.infra.presistence.impl;

import com.case_study.identity.infra.model.UserRepository;
import com.case_study.identity.infra.presistence.dao.JPAUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {

    @Autowired
    JPAUserRepository jpaUserRepository;
}
