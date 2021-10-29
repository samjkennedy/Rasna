package com.skennedy.bixbite.typebinding;

import java.util.Arrays;
import java.util.Iterator;

public class BoundBinaryExpression implements BoundExpression {

    private final BoundExpression left;
    private final BoundBinaryOperator operator;
    private final BoundExpression right;

    public BoundBinaryExpression(BoundExpression left, BoundBinaryOperator operator, BoundExpression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.BINARY_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return operator.getReturnType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Arrays.asList(left, operator, right).iterator();
    }

    public BoundExpression getLeft() {
        return left;
    }

    public BoundBinaryOperator getOperator() {
        return operator;
    }

    public BoundExpression getRight() {
        return right;
    }
}
