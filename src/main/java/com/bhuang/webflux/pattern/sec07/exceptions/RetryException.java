package com.bhuang.webflux.pattern.sec07.exceptions;

public class RetryException extends RuntimeException{
    public RetryException(String s, int value) {
        super(s + " : " + value);
    }
}
