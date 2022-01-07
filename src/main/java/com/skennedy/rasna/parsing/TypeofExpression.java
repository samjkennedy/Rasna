package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class TypeofExpression extends Expression {

    private final IdentifierExpression typeofKeyword;
    private final IdentifierExpression openParen;
    private final Expression expression;
    private final IdentifierExpression closeParen;

    public TypeofExpression(IdentifierExpression typeofKeyword, IdentifierExpression openParen, Expression expression, IdentifierExpression closeParen) {
        this.typeofKeyword = typeofKeyword;
        this.openParen = openParen;
        this.expression = expression;
        this.closeParen = closeParen;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.TYPEOF_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)typeofKeyword, openParen, expression, closeParen).iterator();
    }

    public IdentifierExpression getTypeofKeyword() {
        return typeofKeyword;
    }

    public IdentifierExpression getOpenParen() {
        return openParen;
    }

    public Expression getExpression() {
        return expression;
    }

    public IdentifierExpression getCloseParen() {
        return closeParen;
    }
}
