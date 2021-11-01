package com.skennedy.lazuli.exceptions;

public class UndefinedFunctionException extends RuntimeException {
    public UndefinedFunctionException(String function) {
        super("Function " + function + " is not defined within the scope");
    }
}
