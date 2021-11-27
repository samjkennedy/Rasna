package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Iterator;

public class ReduceExpression extends Expression {

    public ReduceExpression(IdentifierExpression reduce, IdentifierExpression openParen, Expression expression, IdentifierExpression closeParen) {
        super();
    }

    @Override
    public ExpressionType getExpressionType() {
        return null;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return null;
    }
}
