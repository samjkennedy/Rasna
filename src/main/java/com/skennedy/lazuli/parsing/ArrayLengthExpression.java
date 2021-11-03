package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class ArrayLengthExpression extends Expression {

    private final IdentifierExpression len;
    private final IdentifierExpression openParen;
    private final Expression expression;
    private final IdentifierExpression closeParen;

    public ArrayLengthExpression(IdentifierExpression len, IdentifierExpression openParen, Expression expression, IdentifierExpression closeParen) {

        this.len = len;
        this.openParen = openParen;
        this.expression = expression;
        this.closeParen = closeParen;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.ARRAY_LEN_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)len, openParen, expression, closeParen).iterator();
    }

    public IdentifierExpression getLen() {
        return len;
    }

    public IdentifierExpression getOpenParen() {
        return openParen;
    }

    public Expression getExpression() {
        return expression;
    }

    public IdentifierExpression getCloseParen() {
        return closeParen;
    }
}
