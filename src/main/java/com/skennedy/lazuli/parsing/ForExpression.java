package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;

public class ForExpression extends Expression {

    private final IdentifierExpression forKeyword;
    private final IdentifierExpression openParen;
    private final TypeExpression typeExpression;
    private final IdentifierExpression identifier;
    private final IdentifierExpression equals;
    private final RangeExpression rangeExpression;
    private final IdentifierExpression byKeyword;
    private final Expression step;
    private Expression guard;
    private final IdentifierExpression closeParen;
    private final Expression body;

    public ForExpression(IdentifierExpression forKeyword,
                         IdentifierExpression openParen,
                         TypeExpression typeExpression,
                         IdentifierExpression identifier,
                         IdentifierExpression equals,
                         RangeExpression rangeExpression,
                         IdentifierExpression byKeyword,
                         Expression step,
                         Expression guard,
                         IdentifierExpression closeParen,
                         Expression body) {

        this.forKeyword = forKeyword;
        this.openParen = openParen;
        this.typeExpression = typeExpression;
        this.identifier = identifier;
        this.equals = equals;
        this.rangeExpression = rangeExpression;
        this.byKeyword = byKeyword;
        this.step = step;
        this.guard = guard;
        this.closeParen = closeParen;
        this.body = body;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.FOR_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode) forKeyword, openParen, typeExpression, identifier, equals, rangeExpression, step, guard, closeParen, body).iterator();
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

    public IdentifierExpression getEquals() {
        return equals;
    }

    public RangeExpression getRangeExpression() {
        return rangeExpression;
    }

    public IdentifierExpression getByKeyword() {
        return byKeyword;
    }

    public Expression getStep() {
        return step;
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
