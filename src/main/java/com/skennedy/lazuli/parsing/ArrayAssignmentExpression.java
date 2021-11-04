package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class ArrayAssignmentExpression extends Expression {

    private final ArrayAccessExpression arrayAccessExpression;
    private final IdentifierExpression equals;
    private final Expression assignment;

    public ArrayAssignmentExpression(ArrayAccessExpression arrayAccessExpression, IdentifierExpression equals, Expression assignment) {
        this.arrayAccessExpression = arrayAccessExpression;
        this.equals = equals;
        this.assignment = assignment;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.ARRAY_ASSIGNMENT_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)arrayAccessExpression, equals, assignment).iterator();
    }

    public ArrayAccessExpression getArrayAccessExpression() {
        return arrayAccessExpression;
    }

    public IdentifierExpression getEquals() {
        return equals;
    }

    public Expression getAssignment() {
        return assignment;
    }
}
