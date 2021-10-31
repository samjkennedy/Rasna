package com.skennedy.lazuli.typebinding;

import java.util.Collections;
import java.util.Iterator;

public class BoundTypeofExpression implements BoundExpression {

    private BoundExpression expression;

    public BoundTypeofExpression(BoundExpression expression) {
        this.expression = expression;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.TYPEOF_INTRINSIC;
    }

    @Override
    public TypeSymbol getType() {
        return TypeSymbol.TYPE;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.singleton(expression).iterator();
    }

    public BoundExpression getExpression() {
        return expression;
    }
}
