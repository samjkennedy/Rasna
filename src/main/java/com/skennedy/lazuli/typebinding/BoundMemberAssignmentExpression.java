package com.skennedy.lazuli.typebinding;

import java.util.Arrays;
import java.util.Iterator;

public class BoundMemberAssignmentExpression implements BoundExpression {

    private final BoundMemberAccessorExpression memberAccessorExpression;
    private final BoundExpression assignment;

    public BoundMemberAssignmentExpression(BoundMemberAccessorExpression memberAccessorExpression, BoundExpression assignment) {
        this.memberAccessorExpression = memberAccessorExpression;
        this.assignment = assignment;
    }
    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.MEMBER_ASSIGNMENT_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return assignment.getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Arrays.asList(memberAccessorExpression, assignment).iterator();
    }

    public BoundMemberAccessorExpression getMemberAccessorExpression() {
        return memberAccessorExpression;
    }

    public BoundExpression getAssignment() {
        return assignment;
    }
}
