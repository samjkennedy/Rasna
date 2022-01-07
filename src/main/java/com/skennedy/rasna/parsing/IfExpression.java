package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

public class IfExpression extends Expression {

    private final IdentifierExpression ifKeyword;
    private final IdentifierExpression openParen;
    private final Expression condition;
    private final IdentifierExpression closeParen;
    private final Expression body;
    private final IdentifierExpression elseKeyword;
    private Expression elseBody;

    public IfExpression(IdentifierExpression ifKeyword, IdentifierExpression openParen, Expression condition, IdentifierExpression closeParen, Expression body) {
        this.ifKeyword = ifKeyword;
        this.openParen = openParen;
        this.condition = condition;
        this.closeParen = closeParen;
        this.body = body;
        this.elseKeyword = null;
        this.elseBody = null;
    }

    public IfExpression(IdentifierExpression ifKeyword, IdentifierExpression openParen, Expression condition, IdentifierExpression closeParen, Expression body, IdentifierExpression elseKeyword, Expression elseBody) {
        this.ifKeyword = ifKeyword;
        this.openParen = openParen;
        this.condition = condition;
        this.closeParen = closeParen;
        this.body = body;
        this.elseKeyword = elseKeyword;
        this.elseBody = elseBody;
    }

    public IdentifierExpression getIfKeyword() {
        return ifKeyword;
    }

    public IdentifierExpression getOpenParen() {
        return openParen;
    }

    public Expression getCondition() {
        return condition;
    }

    public IdentifierExpression getCloseParen() {
        return closeParen;
    }

    public Expression getBody() {
        return body;
    }

    public Expression getElseBody() {
        return elseBody;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.IF_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Stream.of((SyntaxNode)ifKeyword, openParen, condition, closeParen, body, elseKeyword, elseBody).filter(Objects::nonNull).iterator();
    }
}
