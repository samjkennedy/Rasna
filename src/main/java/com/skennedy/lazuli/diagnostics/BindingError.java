package com.skennedy.lazuli.diagnostics;

import com.skennedy.lazuli.lexing.model.Token;
import com.skennedy.lazuli.typebinding.TypeSymbol;

public class BindingError {

    private final String message;
    private final TextSpan span;

    public BindingError(String message, TextSpan span) {

        this.message = message;
        this.span = span;
    }

    public static BindingError raiseTypeMismatch(TypeSymbol expected, TypeSymbol actual, TextSpan span) {
        return new BindingError("Expected type `" + expected.getName() + "` but got `" + actual.getName() + "`", span);
    }

    //TODO: Have the location the variable already declared
    public static BindingError raiseVariableAlreadyDeclared(String name, TextSpan span) {
        return new BindingError("Variable " + name + " is already declared in the scope", span);
    }

    public static BindingError raiseFunctionAlreadyDeclared(String name, TextSpan span) {
        return new BindingError("Function " + name + " is already declared in the scope", span);
    }

    public static BindingError raiseTypeAlreadyDeclared(String name, TextSpan span) {
        return new BindingError("Type " + name + " is already declared in the scope", span);
    }

    public static BindingError raiseUnknownType(String name, TextSpan span) {
        return new BindingError("Type " + name + " is not declared in the scope", span);
    }

    public static BindingError raiseUnknownNamespace(String name, TextSpan span) {
        return new BindingError("Namespace " + name + " is not declared in the scope", span);
    }

    public String getMessage() {
        return message;
    }

    public TextSpan getSpan() {
        return span;
    }
}
