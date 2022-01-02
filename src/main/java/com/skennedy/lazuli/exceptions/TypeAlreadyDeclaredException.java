package com.skennedy.lazuli.exceptions;

public class TypeAlreadyDeclaredException extends RuntimeException {
    public TypeAlreadyDeclaredException(String function) {
        super("Type " + function + " is already defined within the scope");
    }
}
