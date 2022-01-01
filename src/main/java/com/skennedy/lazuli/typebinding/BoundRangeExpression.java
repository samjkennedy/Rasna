package com.skennedy.lazuli.typebinding;

import com.skennedy.lazuli.exceptions.TypeMismatchException;

import java.util.Arrays;
import java.util.Iterator;

public class BoundRangeExpression implements BoundExpression {

    private final BoundExpression lowerBound;
    private final BoundExpression upperBound;

    public BoundRangeExpression(BoundExpression lowerBound, BoundExpression upperBound) {
        if (!lowerBound.getType().isAssignableFrom(upperBound.getType())) {
            throw new TypeMismatchException(lowerBound.getType(), upperBound.getType());
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public BoundExpression getLowerBound() {
        return lowerBound;
    }

    public BoundExpression getUpperBound() {
        return upperBound;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.RANGE_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return lowerBound.getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Arrays.asList(lowerBound, upperBound).iterator();
    }
}
