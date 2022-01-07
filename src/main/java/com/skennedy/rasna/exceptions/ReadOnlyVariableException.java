package com.skennedy.rasna.exceptions;

public class ReadOnlyVariableException extends RuntimeException {

    public ReadOnlyVariableException(String name) {
        super("Variable " + name + " is constant and cannot be reassigned");
    }
}
