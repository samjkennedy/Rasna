package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class ForInExpression extends Expression {

    private final IdentifierExpression forKeyword;
    private final IdentifierExpression openParen;
    private final TypeExpression typeExpression;
    private final IdentifierExpression identifier;
    private final IdentifierExpression inKeyword;
    private final Expression iterable;
    private final Expression guard;
    private final IdentifierExpression closeParen;
    private final Expression body;

    public ForInExpression(IdentifierExpression forKeyword, IdentifierExpression openParen, TypeExpression typeExpression, IdentifierExpression identifier, IdentifierExpression inKeyword, Expression iterable, Expression guard, IdentifierExpression closeParen, Expression body) {

        this.forKeyword = forKeyword;
        this.openParen = openParen;
        this.typeExpression = typeExpression;
        this.identifier = identifier;
        this.inKeyword = inKeyword;
        this.iterable = iterable;
        this.guard = guard;
        this.closeParen = closeParen;
        this.body = body;
    }


    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.FOR_IN_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)forKeyword,  openParen, typeExpression, identifier, inKeyword, iterable, guard, closeParen, body).iterator();
    }

    public IdentifierExpression getForKeyword() {
        return forKeyword;
    }

    public IdentifierExpression getOpenParen() {
        return openParen;
    }

    public TypeExpression getTypeExpression() {
        return typeExpression;
    }

    public IdentifierExpression getIdentifier() {
        return identifier;
    }

    public IdentifierExpression getInKeyword() {
        return inKeyword;
    }

    public Expression getIterable() {
        return iterable;
    }

    public Expression getGuard() {
        return guard;
    }

    public IdentifierExpression getCloseParen() {
        return closeParen;
    }

    public Expression getBody() {
        return body;
    }
}
