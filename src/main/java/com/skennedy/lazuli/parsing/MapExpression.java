package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class MapExpression extends Expression {

    private final IdentifierExpression map;
    private final IdentifierExpression openParen;
    private final Expression mapFunction;
    private final IdentifierExpression comma;
    private final Expression expression;
    private final IdentifierExpression closeParen;

    public MapExpression(IdentifierExpression map, IdentifierExpression openParen, Expression mapFunction, IdentifierExpression comma, Expression expression, IdentifierExpression closeParen) {
        this.map = map;
        this.openParen = openParen;
        this.mapFunction = mapFunction;
        this.comma = comma;
        this.expression = expression;
        this.closeParen = closeParen;
    }

    public IdentifierExpression getMap() {
        return map;
    }

    public IdentifierExpression getOpenParen() {
        return openParen;
    }

    public Expression getMapFunction() {
        return mapFunction;
    }

    public IdentifierExpression getComma() {
        return comma;
    }

    public Expression getExpression() {
        return expression;
    }

    public IdentifierExpression getCloseParen() {
        return closeParen;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.MAP_EXPRESSION;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)map, openParen, mapFunction, comma, expression, closeParen).iterator();
    }
}
