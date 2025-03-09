package com.securefromscratch.busybee.exceptions;

public class InvalidTokenException extends Throwable {
    public InvalidTokenException() {
        super("Invalid token");
    }
}
