package com.skennedy.rasna.typebinding;

import com.skennedy.rasna.diagnostics.BindingError;

import java.util.List;

public class BoundProgram {

    private final List<BoundExpression> expressions;
    private final List<BindingError> errors;
    private List<BindingWarning> warnings;

    public BoundProgram(List<BoundExpression> expressions, List<BindingError> errors, List<BindingWarning> warnings) {
        this.expressions = expressions;
        this.errors = errors;
        this.warnings = warnings;
    }

    public List<BoundExpression> getExpressions() {
        return expressions;
    }

    public List<BindingError> getErrors() {
        return errors;
    }

    public List<BindingWarning> getWarnings() {
        return warnings;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
}
