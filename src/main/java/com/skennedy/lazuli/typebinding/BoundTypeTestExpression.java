package com.skennedy.lazuli.typebinding;

import java.util.Collections;
import java.util.Iterator;

public class BoundTypeTestExpression implements BoundExpression {

    private final BoundExpression expression;
    private final TypeSymbol typeLiteral;

    public BoundTypeTestExpression(BoundExpression expression, TypeSymbol typeLiteral) {
        this.expression = expression;
        this.typeLiteral = typeLiteral;
    }

    public BoundExpression getExpression() {
        return expression;
    }

    public TypeSymbol getTypeLiteral() {
        return typeLiteral;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.TYPE_TEST_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return TypeSymbol.BOOL;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.singleton(expression).iterator();
    }
}
