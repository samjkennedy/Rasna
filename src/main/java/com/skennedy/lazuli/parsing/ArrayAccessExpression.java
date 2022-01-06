package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class ArrayAccessExpression extends Expression {

    private final IdentifierExpression identifier;
    private final IdentifierExpression openBrace;
    private final Expression index;
    private final IdentifierExpression closeBrace;

    public ArrayAccessExpression(IdentifierExpression identifier, IdentifierExpression openBrace, Expression index, IdentifierExpression closeBrace) {
        this.identifier = identifier;
        this.openBrace = openBrace;
        this.index = index;
        this.closeBrace = closeBrace;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.ARRAY_ACCESS_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)identifier, openBrace, index, closeBrace).iterator();
    }

    public IdentifierExpression getIdentifier() {
        return identifier;
    }

    public IdentifierExpression getOpenBrace() {
        return openBrace;
    }

    public Expression getIndex() {
        return index;
    }

    public IdentifierExpression getCloseBrace() {
        return closeBrace;
    }
}
