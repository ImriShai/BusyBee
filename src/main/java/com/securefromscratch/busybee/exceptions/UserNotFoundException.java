package com.securefromscratch.busybee.exceptions;

public class UserNotFoundException
extends Throwable {
    public UserNotFoundException(String userNotFound) {
        super("The user " + userNotFound + " was not found");
    }
}
