package com.skennedy.rasna.typebinding;

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
        if (value instanceof Integer) {
            return TypeSymbol.INT;
        } else if (value instanceof Boolean) {
            return TypeSymbol.BOOL;
        } else if (value instanceof Double) {
            return TypeSymbol.REAL;
        }  else if (value instanceof String) {
            return TypeSymbol.STRING;
        } else {
            throw new IllegalStateException("Unsupported type of literal: " + value.getClass());
        }
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.emptyIterator();
    }

    @Override
    public boolean isConstExpression() {
        return true;
    }

    @Override
    public Object getConstValue() {
        return value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "Null";
    }
}
