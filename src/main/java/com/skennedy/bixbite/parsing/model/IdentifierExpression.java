package com.skennedy.bixbite.parsing.model;

import com.skennedy.bixbite.lexing.model.TokenType;
import com.skennedy.bixbite.parsing.Expression;

import java.util.Collections;
import java.util.Iterator;

public class IdentifierExpression extends Expression {

    //TODO: add other things like location + text
    private final TokenType tokenType;
    private final Object value;

    public IdentifierExpression(TokenType tokenType, Object value) {
        this.tokenType = tokenType;
        this.value = value;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.IDENTIFIER_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Collections.emptyIterator();
    }
}
