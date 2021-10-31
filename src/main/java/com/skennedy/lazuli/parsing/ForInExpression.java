package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class ForInExpression extends Expression {

    private final IdentifierExpression forKeyword;
    private final IdentifierExpression openParen;
    private final IdentifierExpression typeKeyword;
    private final IdentifierExpression identifier;
    private final IdentifierExpression inKeyword;
    private final Expression iterable;
    private final Expression range;
    private final IdentifierExpression closeParen;
    private final BlockExpression body;

    public ForInExpression(IdentifierExpression forKeyword, IdentifierExpression openParen, IdentifierExpression typeKeyword, IdentifierExpression identifier, IdentifierExpression inKeyword, Expression iterable, Expression range, IdentifierExpression closeParen, BlockExpression body) {

        this.forKeyword = forKeyword;
        this.openParen = openParen;
        this.typeKeyword = typeKeyword;
        this.identifier = identifier;
        this.inKeyword = inKeyword;
        this.iterable = iterable;
        this.range = range;
        this.closeParen = closeParen;
        this.body = body;
    }


    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.FOR_IN_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)forKeyword,  openParen, typeKeyword, identifier, inKeyword, iterable, range, closeParen, body).iterator();
    }

    public IdentifierExpression getForKeyword() {
        return forKeyword;
    }

    public IdentifierExpression getOpenParen() {
        return openParen;
    }

    public IdentifierExpression getTypeKeyword() {
        return typeKeyword;
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

    public Expression getRange() {
        return range;
    }

    public IdentifierExpression getCloseParen() {
        return closeParen;
    }

    public BlockExpression getBody() {
        return body;
    }
}
