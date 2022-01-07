package com.skennedy.rasna.typebinding;

import java.util.Arrays;
import java.util.Iterator;

public class BoundMapExpression implements BoundExpression {

    private final BoundExpression mapperFunction;
    private final BoundExpression operand;

    public BoundMapExpression(BoundExpression mapperFunction, BoundExpression operand) {
        this.mapperFunction = mapperFunction;
        this.operand = operand;
    }

    public BoundExpression getMapperFunction() {
        return mapperFunction;
    }

    public BoundExpression getOperand() {
        return operand;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.MAP_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return operand.getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Arrays.asList(mapperFunction, operand).iterator();
    }
}
