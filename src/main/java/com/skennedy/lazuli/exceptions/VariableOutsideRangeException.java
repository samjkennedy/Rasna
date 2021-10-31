package com.skennedy.lazuli.exceptions;

public class VariableOutsideRangeException extends RuntimeException {
    public VariableOutsideRangeException(String name) {
        super("Variable " + name + " is outside range");
    }
}
