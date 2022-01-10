package com.skennedy.rasna.typebinding;

import java.util.Arrays;
import java.util.Iterator;

public class BoundUnaryExpression implements BoundExpression {

    private final BoundUnaryOperator operator;
    private final BoundExpression operand;

    public BoundUnaryExpression(BoundUnaryOperator operator, BoundExpression operand) {
        this.operator = operator;
        this.operand = operand;
    }

    public BoundUnaryOperator getOperator() {
        return operator;
    }

    public BoundExpression getOperand() {
        return operand;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.UNARY_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return operator.getReturnType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Arrays.asList(operand, operand).iterator();
    }
}
