package com.skennedy.lazuli.exceptions;

import com.skennedy.lazuli.typebinding.TypeSymbol;

public class TypeMismatchException extends RuntimeException {

    public TypeMismatchException(TypeSymbol expected, TypeSymbol actual) {
        super("Type mismatch, expected `" + expected.getName() + "` but got `" + actual.getName() + "`");
    }
}
