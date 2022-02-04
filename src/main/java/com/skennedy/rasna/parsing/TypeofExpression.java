package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class TypeofExpression extends Expression {

    private final IdentifierExpression typeofKeyword;
    private final Expression expression;

    public TypeofExpression(IdentifierExpression typeofKeyword, Expression expression) {
        this.typeofKeyword = typeofKeyword;
        this.expression = expression;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.TYPEOF_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)typeofKeyword, expression).iterator();
    }

    public IdentifierExpression getTypeofKeyword() {
        return typeofKeyword;
    }

    public Expression getExpression() {
        return expression;
    }
}
