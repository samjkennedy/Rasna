package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArrayLiteralExpression extends Expression {

    private final IdentifierExpression openSquareBrace;
    private final List<Expression> elements;
    private final IdentifierExpression closeSquareBrace;

    public ArrayLiteralExpression(IdentifierExpression openSquareBrace, List<Expression> elements, IdentifierExpression closeSquareBrace) {
        this.openSquareBrace = openSquareBrace;
        this.elements = elements;
        this.closeSquareBrace = closeSquareBrace;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.ARRAY_LITERAL_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {

        List<SyntaxNode> children = new ArrayList<>();

        children.add(openSquareBrace);
        children.addAll(elements);
        children.add(closeSquareBrace);

        return children.iterator();
    }

    public IdentifierExpression getOpenSquareBrace() {
        return openSquareBrace;
    }

    public List<Expression> getElements() {
        return elements;
    }

    public IdentifierExpression getCloseSquareBrace() {
        return closeSquareBrace;
    }
}
