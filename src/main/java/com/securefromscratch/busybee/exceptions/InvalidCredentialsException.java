package com.securefromscratch.busybee.exceptions;

public class InvalidCredentialsException
extends Throwable {
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
