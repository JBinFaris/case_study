package com.case_study.identity.repository.presistence.dao;

import com.case_study.identity.repository.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JPAUserRepository extends JpaRepository<User, Long > {


    User findByEmail(String email);

    }
