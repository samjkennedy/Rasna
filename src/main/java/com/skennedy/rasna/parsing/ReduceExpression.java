package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

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
