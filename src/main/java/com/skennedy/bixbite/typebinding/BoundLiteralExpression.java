package com.skennedy.bixbite.typebinding;

import java.util.Collections;
import java.util.Iterator;

public class BoundLiteralExpression implements BoundExpression {

    private final Object value;

    public BoundLiteralExpression(Object value) {
        this.value = value;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.LITERAL;
    }

    @Override
    public TypeSymbol getType() {
        //TODO: add more types
        return TypeSymbol.INT;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.emptyIterator();
    }

    public Object getValue() {
        return value;
    }
}
