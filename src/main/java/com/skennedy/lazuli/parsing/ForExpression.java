package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class ForExpression extends Expression {

    private final IdentifierExpression forKeyword;
    private final IdentifierExpression openParen;
    private final IdentifierExpression typeKeyword;
    private final IdentifierExpression identifier;
    private final IdentifierExpression equals;
    private final Expression initialiser;
    private final IdentifierExpression toKeyword;
    private final Expression terminator;
    private final IdentifierExpression byKeyword;
    private final Expression step;
    private Expression range;
    private final IdentifierExpression closeParen;
    private final BlockExpression body;

    public ForExpression(IdentifierExpression forKeyword,
                         IdentifierExpression openParen,
                         IdentifierExpression typeKeyword,
                         IdentifierExpression identifier,
                         IdentifierExpression equals,
                         Expression initialiser,
                         IdentifierExpression toKeyword,
                         Expression terminator,
                         IdentifierExpression byKeyword,
                         Expression step,
                         Expression range,
                         IdentifierExpression closeParen,
                         BlockExpression body) {

        this.forKeyword = forKeyword;
        this.openParen = openParen;
        this.typeKeyword = typeKeyword;
        this.identifier = identifier;
        this.equals = equals;
        this.initialiser = initialiser;
        this.toKeyword = toKeyword;
        this.terminator = terminator;
        this.byKeyword = byKeyword;
        this.step = step;
        this.range = range;
        this.closeParen = closeParen;
        this.body = body;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.FOR_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode) forKeyword, openParen, typeKeyword, identifier, equals, initialiser, toKeyword, terminator, step, range, closeParen, body).iterator();
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

    public IdentifierExpression getEquals() {
        return equals;
    }

    public Expression getInitialiser() {
        return initialiser;
    }

    public IdentifierExpression getToKeyword() {
        return toKeyword;
    }

    public Expression getTerminator() {
        return terminator;
    }

    public IdentifierExpression getByKeyword() {
        return byKeyword;
    }

    public Expression getStep() {
        return step;
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
