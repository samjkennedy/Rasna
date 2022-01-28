package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class ArrayAccessExpression extends Expression {

    private final Expression array;
    private final IdentifierExpression openBrace;
    private final Expression index;
    private final IdentifierExpression closeBrace;

    public ArrayAccessExpression(Expression array, IdentifierExpression openBrace, Expression index, IdentifierExpression closeBrace) {
        this.array = array;
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
        return Arrays.asList((SyntaxNode)array, openBrace, index, closeBrace).iterator();
    }

    public Expression getArray() {
        return array;
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
