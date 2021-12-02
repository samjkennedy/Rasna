package com.skennedy.lazuli.typebinding;

import java.util.Collections;
import java.util.Iterator;

public class BoundYieldExpression implements BoundExpression {

    private BoundExpression expression;

    public BoundYieldExpression(BoundExpression expression) {
        this.expression = expression;
    }

    public BoundExpression getExpression() {
        return expression;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.YIELD_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return expression.getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.singleton(expression).iterator();
    }
}
