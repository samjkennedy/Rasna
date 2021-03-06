package com.skennedy.rasna.typebinding;

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
        if (array.getType() instanceof TupleTypeSymbol) {
            return TypeSymbol.ANY;
        }
        if (array.getType() == TypeSymbol.STRING) {
            return TypeSymbol.CHAR;
        }
        return ((ArrayTypeSymbol)array.getType()).getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.singleton(index).iterator();
    }
}
