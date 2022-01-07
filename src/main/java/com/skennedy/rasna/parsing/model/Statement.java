package com.skennedy.rasna.parsing.model;

import com.skennedy.rasna.parsing.Expression;

import java.util.Iterator;

public abstract class Statement implements SyntaxNode {

    private final Expression expression;
    private final IdentifierExpression semicolon;

    protected Statement(Expression expression, IdentifierExpression semicolon) {
        this.expression = expression;
        this.semicolon = semicolon;
    }

    public Expression getExpression() {
        return expression;
    }

    public IdentifierExpression getSemicolon() {
        return semicolon;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return expression.getChildren();
    }
}
