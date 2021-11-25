package com.skennedy.lazuli.typebinding;

import java.util.Arrays;
import java.util.Iterator;

public class BoundForExpression implements BoundExpression {

    private VariableSymbol iterator;
    private final BoundExpression initialiser;
    private final BoundExpression terminator;
    private final BoundExpression step;
    private final BoundExpression guard;
    private final BoundExpression body;

    public BoundForExpression(VariableSymbol iterator, BoundExpression initialiser, BoundExpression terminator, BoundExpression step, BoundExpression guard, BoundExpression body) {
        this.iterator = iterator;
        this.initialiser = initialiser;
        this.terminator = terminator;
        this.step = step;
        this.guard = guard;
        this.body = body;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.FOR;
    }

    @Override
    public TypeSymbol getType() {
        return iterator.getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Arrays.asList(initialiser, terminator, step, guard, body).iterator();
    }

    public VariableSymbol getIterator() {
        return iterator;
    }

    public BoundExpression getInitialiser() {
        return initialiser;
    }

    public BoundExpression getTerminator() {
        return terminator;
    }

    public BoundExpression getStep() {
        return step;
    }

    public BoundExpression getGuard() {
        return guard;
    }

    public BoundExpression getBody() {
        return body;
    }
}
