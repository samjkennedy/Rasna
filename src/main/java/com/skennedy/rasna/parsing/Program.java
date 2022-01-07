package com.skennedy.rasna.parsing;

import com.skennedy.rasna.diagnostics.Error;

import java.util.List;

public class Program {

    private final List<Error> errors;

    private final List<Expression> expressions;

    public Program(List<Error> errors, List<Expression> expressions) {
        this.errors = errors;
        this.expressions = expressions;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
