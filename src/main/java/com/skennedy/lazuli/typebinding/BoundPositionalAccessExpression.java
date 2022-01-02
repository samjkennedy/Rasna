package com.skennedy.lazuli.typebinding;

import java.util.Collections;
import java.util.Iterator;

public class BoundPositionalAccessExpression implements BoundExpression {

    private BoundExpression array;
    private BoundExpression index;

    public BoundPositionalAccessExpression(BoundExpression array, BoundExpression index) {
        this.array = array;
        this.index = index;
    }

    public BoundExpression getArray() {
        return array;
    }

    public BoundExpression getIndex() {
        return index;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.POSITIONAL_ACCESS_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        if (array.getType() == TypeSymbol.TUPLE) {
            return TypeSymbol.VAR;
        }
        return ((ArrayTypeSymbol)array.getType()).getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.singleton(index).iterator();
    }
}
