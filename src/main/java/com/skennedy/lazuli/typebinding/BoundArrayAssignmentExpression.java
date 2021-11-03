package com.skennedy.lazuli.typebinding;

import java.util.Arrays;
import java.util.Iterator;

public class BoundArrayAssignmentExpression implements BoundExpression {

    private final BoundArrayAccessExpression arrayAccessExpression;
    private final BoundExpression assignment;

    public BoundArrayAssignmentExpression(BoundArrayAccessExpression arrayAccessExpression, BoundExpression assignment) {
        this.arrayAccessExpression = arrayAccessExpression;
        this.assignment = assignment;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.ARRAY_ASSIGNMENT_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return assignment.getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Arrays.asList(arrayAccessExpression, assignment).iterator();
    }

    public BoundArrayAccessExpression getArrayAccessExpression() {
        return arrayAccessExpression;
    }

    public BoundExpression getAssignment() {
        return assignment;
    }
}
