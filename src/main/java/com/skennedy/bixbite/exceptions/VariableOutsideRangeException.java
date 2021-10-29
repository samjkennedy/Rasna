package com.skennedy.bixbite.exceptions;

public class VariableOutsideRangeException extends RuntimeException {
    public VariableOutsideRangeException(String name) {
        super("Variable " + name + " is outside range");
    }
}
