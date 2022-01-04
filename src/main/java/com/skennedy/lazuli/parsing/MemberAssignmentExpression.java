package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class MemberAssignmentExpression extends Expression {

    private final MemberAccessorExpression memberAccessorExpression;
    private final IdentifierExpression equals;
    private final Expression assignment;

    public MemberAssignmentExpression(MemberAccessorExpression memberAccessorExpression, IdentifierExpression equals, Expression assignment) {
        this.memberAccessorExpression = memberAccessorExpression;
        this.equals = equals;
        this.assignment = assignment;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.MEMBER_ASSIGNMENT_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)memberAccessorExpression, equals, assignment).iterator();
    }

    public MemberAccessorExpression getMemberAccessorExpression() {
        return memberAccessorExpression;
    }

    public IdentifierExpression getEquals() {
        return equals;
    }

    public Expression getAssignment() {
        return assignment;
    }
}
