package com.securefromscratch.busybee.exceptions;

public class UserAlreadyExistException extends Throwable {
    public UserAlreadyExistException(String userAlreadyExists) {
        super("The user " + userAlreadyExists + " already exists");
    }
}
