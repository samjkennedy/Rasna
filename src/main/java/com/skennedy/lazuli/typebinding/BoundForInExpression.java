package com.skennedy.lazuli.typebinding;

import java.util.Arrays;
import java.util.Iterator;

public class BoundForInExpression implements BoundExpression {

    private final VariableSymbol variable;
    private final BoundExpression iterable;
    private final BoundExpression range;
    private final BoundExpression body;

    public BoundForInExpression(VariableSymbol variable, BoundExpression iterable, BoundExpression range, BoundExpression body) {
        this.variable = variable;
        this.iterable = iterable;
        this.range = range;
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
        return Arrays.asList(iterable, range, body).iterator();
    }

    public VariableSymbol getVariable() {
        return variable;
    }

    public BoundExpression getIterable() {
        return iterable;
    }

    public BoundExpression getRange() {
        return range;
    }

    public BoundExpression getBody() {
        return body;
    }
}
