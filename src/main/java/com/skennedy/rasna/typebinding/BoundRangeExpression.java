package com.skennedy.rasna.typebinding;

import com.skennedy.rasna.exceptions.TypeMismatchException;

import java.util.Arrays;
import java.util.Iterator;

public class BoundRangeExpression implements BoundExpression {

    private final BoundExpression lowerBound;
    private final BoundExpression upperBound;
    private final BoundExpression step;

    public BoundRangeExpression(BoundExpression lowerBound, BoundExpression upperBound, BoundExpression step) {
        this.step = step;
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

    public BoundExpression getStep() {
        return step;
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
        return Arrays.asList(lowerBound, upperBound, step).iterator();
    }
}
