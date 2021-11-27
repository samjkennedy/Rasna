package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class ArrayDeclarationExpression extends Expression {

    private final TypeExpression typeExpression;
    private final IdentifierExpression openSquareBrace;
    private final Expression elementCount;
    private final IdentifierExpression closeSquareBrace;

    public ArrayDeclarationExpression(TypeExpression typeExpression, IdentifierExpression openSquareBrace, Expression elementCount, IdentifierExpression closeSquareBrace) {
        this.typeExpression = typeExpression;
        this.openSquareBrace = openSquareBrace;
        this.elementCount = elementCount;
        this.closeSquareBrace = closeSquareBrace;
    }

    public TypeExpression getTypeExpression() {
        return typeExpression;
    }

    public IdentifierExpression getOpenSquareBrace() {
        return openSquareBrace;
    }

    public Expression getElementCount() {
        return elementCount;
    }

    public IdentifierExpression getCloseSquareBrace() {
        return closeSquareBrace;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.ARRAY_DECLARATION_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)typeExpression, openSquareBrace, elementCount, closeSquareBrace).iterator();
    }
}
