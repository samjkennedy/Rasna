package com.skennedy.lazuli.exceptions;

public class UndefinedVariableException extends RuntimeException {

    public UndefinedVariableException(String variable) {
        super("Variable " + variable + " is not defined within the scope");
    }
}
