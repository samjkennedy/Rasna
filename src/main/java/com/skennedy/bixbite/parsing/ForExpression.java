package com.skennedy.bixbite.parsing;

import com.skennedy.bixbite.parsing.model.ExpressionType;
import com.skennedy.bixbite.parsing.model.IdentifierExpression;
import com.skennedy.bixbite.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class ForExpression extends Expression {

    private final IdentifierExpression forKeyword;
    private final IdentifierExpression openParen;
    private final IdentifierExpression varKeyword;
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
                         IdentifierExpression varKeyword,
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
        this.varKeyword = varKeyword;
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
        return Arrays.asList((SyntaxNode) forKeyword, openParen, varKeyword, identifier, equals, initialiser, toKeyword, terminator, step, range, closeParen, body).iterator();
    }

    public IdentifierExpression getForKeyword() {
        return forKeyword;
    }

    public IdentifierExpression getOpenParen() {
        return openParen;
    }

    public IdentifierExpression getVarKeyword() {
        return varKeyword;
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
