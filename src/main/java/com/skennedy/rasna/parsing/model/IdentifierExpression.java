package com.skennedy.rasna.parsing.model;

import com.skennedy.rasna.diagnostics.TextSpan;
import com.skennedy.rasna.lexing.model.Location;
import com.skennedy.rasna.lexing.model.Token;
import com.skennedy.rasna.lexing.model.TokenType;
import com.skennedy.rasna.parsing.Expression;

import java.util.Collections;
import java.util.Iterator;

public class IdentifierExpression extends Expression {

    //TODO: add other things like location + text
    private final Token token;
    private final TokenType tokenType;
    private final Object value;

    public IdentifierExpression(Token token, TokenType tokenType, Object value) {
        this.token = token;
        this.tokenType = tokenType;
        this.value = value;
    }

    public Token getToken() {
        return token;
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

    @Override
    public TextSpan getSpan() {
        return new TextSpan(token.getLocation(), Location.fromOffset(token.getLocation(), (String.valueOf(value)).length() - 1));
    }

    @Override
    public String toString() {
        return "IdentifierExpression<" + getValue() + ">";
    }
}
