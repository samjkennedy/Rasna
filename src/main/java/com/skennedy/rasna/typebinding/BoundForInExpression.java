package com.skennedy.rasna.typebinding;

import java.util.Arrays;
import java.util.Iterator;

public class BoundForInExpression implements BoundExpression {

    private final VariableSymbol variable;
    private final BoundExpression iterable;
    private final BoundExpression guard;
    private final BoundExpression body;

    public BoundForInExpression(VariableSymbol variable, BoundExpression iterable, BoundExpression guard, BoundExpression body) {
        this.variable = variable;
        this.iterable = iterable;
        this.guard = guard;
        this.body = body;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.FOR_IN;
    }

    @Override
    public TypeSymbol getType() {
        return iterable.getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Arrays.asList(iterable, guard, body).iterator();
    }

    public VariableSymbol getVariable() {
        return variable;
    }

    public BoundExpression getIterable() {
        return iterable;
    }

    public BoundExpression getGuard() {
        return guard;
    }

    public BoundExpression getBody() {
        return body;
    }
}
