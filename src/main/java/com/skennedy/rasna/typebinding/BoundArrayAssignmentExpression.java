package com.skennedy.rasna.typebinding;

import java.util.Arrays;
import java.util.Iterator;

public class BoundArrayAssignmentExpression implements BoundExpression {

    private final BoundPositionalAccessExpression arrayAccessExpression;
    private final BoundExpression assignment;

    public BoundArrayAssignmentExpression(BoundPositionalAccessExpression arrayAccessExpression, BoundExpression assignment) {
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

    public BoundPositionalAccessExpression getArrayAccessExpression() {
        return arrayAccessExpression;
    }

    public BoundExpression getAssignment() {
        return assignment;
    }
}
