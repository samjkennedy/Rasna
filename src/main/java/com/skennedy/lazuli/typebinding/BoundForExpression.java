package com.skennedy.lazuli.typebinding;

import java.util.Arrays;
import java.util.Iterator;

public class BoundForExpression implements BoundExpression {

    private VariableSymbol iterator;
    private final BoundRangeExpression rangeExpression;
    private final BoundExpression step;
    private final BoundExpression guard;
    private final BoundExpression body;

    public BoundForExpression(VariableSymbol iterator, BoundRangeExpression rangeExpression, BoundExpression step, BoundExpression guard, BoundExpression body) {
        this.iterator = iterator;
        this.rangeExpression = rangeExpression;
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
        return body.getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Arrays.asList(rangeExpression, step, guard, body).iterator();
    }

    public VariableSymbol getIterator() {
        return iterator;
    }

    public BoundRangeExpression getRangeExpression() {
        return rangeExpression;
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
