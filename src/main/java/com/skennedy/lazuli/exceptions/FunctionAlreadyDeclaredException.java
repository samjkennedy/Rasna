package com.skennedy.lazuli.exceptions;

public class FunctionAlreadyDeclaredException extends RuntimeException {
    public FunctionAlreadyDeclaredException(String function) {
        super("Function " + function + " is already defined within the scope");
    }
}
