package com.securefromscratch.busybee.exceptions;

public class ExpiredTokenException  extends Throwable {
    public ExpiredTokenException() {
        super("Expired token");
    }
}
