package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class ArrayDeclarationExpression extends Expression {

    private final IdentifierExpression arrayKeyword;
    private final IdentifierExpression openSquare;
    private final Expression elementCount;
    private final IdentifierExpression comma;
    private final TypeExpression typeExpression;
    private final IdentifierExpression closeSquare;

    public ArrayDeclarationExpression(IdentifierExpression arrayKeyword, IdentifierExpression openSquare, Expression elementCount, IdentifierExpression comma, TypeExpression typeExpression, IdentifierExpression closeSquare) {

        this.arrayKeyword = arrayKeyword;
        this.openSquare = openSquare;
        this.elementCount = elementCount;
        this.comma = comma;
        this.typeExpression = typeExpression;
        this.closeSquare = closeSquare;
    }

    public IdentifierExpression getArrayKeyword() {
        return arrayKeyword;
    }

    public IdentifierExpression getOpenSquare() {
        return openSquare;
    }

    public Expression getElementCount() {
        return elementCount;
    }

    public IdentifierExpression getComma() {
        return comma;
    }

    public TypeExpression getTypeExpression() {
        return typeExpression;
    }

    public IdentifierExpression getCloseSquare() {
        return closeSquare;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.ARRAY_DECLARATION_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)arrayKeyword, openSquare, elementCount, comma, typeExpression, closeSquare).iterator();
    }
}
