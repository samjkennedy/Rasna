package com.skennedy.lazuli.typebinding;

import java.util.Collections;
import java.util.Iterator;

public class BoundCastExpression implements BoundExpression {

    private final BoundExpression expression;
    private final TypeSymbol type;

    public BoundCastExpression(BoundExpression expression, TypeSymbol type) {
        this.expression = expression;
        this.type = type;
    }

    public BoundExpression getExpression() {
        return expression;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.CAST_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return type;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.singleton(expression).iterator();
    }
}
