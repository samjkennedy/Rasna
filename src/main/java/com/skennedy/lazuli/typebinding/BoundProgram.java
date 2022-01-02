package com.skennedy.lazuli.typebinding;

import com.skennedy.lazuli.diagnostics.BindingError;
import com.skennedy.lazuli.diagnostics.Error;

import java.util.List;

public class BoundProgram {

    private final List<BoundExpression> expressions;
    private final List<BindingError> errors;

    public BoundProgram(List<BoundExpression> expressions, List<BindingError> errors) {
        this.expressions = expressions;
        this.errors = errors;
    }

    public List<BoundExpression> getExpressions() {
        return expressions;
    }

    public List<BindingError> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
