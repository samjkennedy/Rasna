package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class ParenthesisedExpression extends Expression {

    private final IdentifierExpression openParen;
    private final Expression expression;
    private final IdentifierExpression closeParen;

    public ParenthesisedExpression(IdentifierExpression openParen, Expression expression, IdentifierExpression closeParen) {
        this.openParen = openParen;
        this.expression = expression;
        this.closeParen = closeParen;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.PARENTHESISED_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)openParen, expression, closeParen).iterator();
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
