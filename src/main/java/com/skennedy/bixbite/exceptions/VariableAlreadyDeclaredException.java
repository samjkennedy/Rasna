package com.skennedy.bixbite.exceptions;


public class VariableAlreadyDeclaredException extends RuntimeException {

    public VariableAlreadyDeclaredException(String variable) {
        super("Variable " + variable + " is already defined within the scope");
    }
}
