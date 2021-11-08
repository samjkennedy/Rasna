package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Iterator;
import java.util.List;

public class TupleLiteralExpression extends Expression {

    private final IdentifierExpression openParen;
    private final List<Expression> elements;
    private final IdentifierExpression closeParen;

    public TupleLiteralExpression(IdentifierExpression openParen, List<Expression> elements, IdentifierExpression closeParen) {
        this.openParen = openParen;
        this.elements = elements;
        this.closeParen = closeParen;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.TUPLE_LITERAL_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return null;
    }

    public IdentifierExpression getOpenParen() {
        return openParen;
    }

    public List<Expression> getElements() {
        return elements;
    }

    public IdentifierExpression getCloseParen() {
        return closeParen;
    }
}
