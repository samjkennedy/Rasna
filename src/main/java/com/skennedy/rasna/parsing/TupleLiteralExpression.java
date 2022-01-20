package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.ArrayList;
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
        List<SyntaxNode> children = new ArrayList<>();
        children.add(openParen);
        children.addAll(elements);
        children.add(closeParen);

        return children.iterator();
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
