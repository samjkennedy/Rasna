package com.skennedy.bixbite.parsing;

import com.skennedy.bixbite.parsing.model.ExpressionType;
import com.skennedy.bixbite.parsing.model.IdentifierExpression;
import com.skennedy.bixbite.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class AssignmentExpression extends Expression {

    private final IdentifierExpression identifier;
    private final IdentifierExpression equals;
    private final Expression assignment;

    public AssignmentExpression(IdentifierExpression identifier, IdentifierExpression equals, Expression assignment) {
        this.identifier = identifier;
        this.equals = equals;
        this.assignment = assignment;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.ASSIGNMENT_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)identifier, equals, assignment).iterator();
    }

    public IdentifierExpression getIdentifier() {
        return identifier;
    }

    public IdentifierExpression getEquals() {
        return equals;
    }

    public Expression getAssignment() {
        return assignment;
    }
}
