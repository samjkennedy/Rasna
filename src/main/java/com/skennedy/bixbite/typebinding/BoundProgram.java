package com.skennedy.bixbite.typebinding;

import com.skennedy.bixbite.diagnostics.Error;

import java.util.List;

public class BoundProgram {

    private final List<BoundExpression> expressions;
    private final List<Error> errors;

    public BoundProgram(List<BoundExpression> expressions, List<Error> errors) {
        this.expressions = expressions;
        this.errors = errors;
    }

    public List<BoundExpression> getExpressions() {
        return expressions;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
