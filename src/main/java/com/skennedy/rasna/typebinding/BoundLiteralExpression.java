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
        if (value == null) {
            return TypeSymbol.UNIT;
        }
        if (value instanceof Character) {
            return TypeSymbol.CHAR;
        }
        if (value instanceof Integer) {
            return TypeSymbol.INT;
        }
        if (value instanceof Boolean) {
            return TypeSymbol.BOOL;
        }
        if (value instanceof Double) {
            return TypeSymbol.REAL;
        }
        if (value instanceof String) {
            return TypeSymbol.STRING;
        }
        throw new IllegalStateException("Unsupported type of literal: " + value.getClass());
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
