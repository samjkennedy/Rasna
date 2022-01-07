package com.skennedy.rasna.exceptions;

import com.skennedy.rasna.typebinding.TypeSymbol;

public class TypeMismatchException extends RuntimeException {

    public TypeMismatchException(TypeSymbol expected, TypeSymbol actual) {
        super("Type mismatch, expected `" + expected + "` but got `" + actual + "`");
    }
}
