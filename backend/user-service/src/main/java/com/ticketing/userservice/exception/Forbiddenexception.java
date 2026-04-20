package com.ticketing.userservice.exception;

public class Forbiddenexception extends RuntimeException {
    public Forbiddenexception(String message) {
        super(message);
    }
}
