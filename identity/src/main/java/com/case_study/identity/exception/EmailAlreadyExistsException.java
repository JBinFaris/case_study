package com.case_study.identity.exception;

import lombok.Builder;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("The email: '" + email + "' is already linked with another user");;
    }
}
