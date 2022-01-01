package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class RangeExpression extends Expression {

    private final Expression lowerBound;
    private final IdentifierExpression toKeyword;
    private final Expression upperBound;

    public RangeExpression(Expression lowerBound, IdentifierExpression toKeyword, Expression upperBound) {
        this.lowerBound = lowerBound;
        this.toKeyword = toKeyword;
        this.upperBound = upperBound;
    }

    public Expression getLowerBound() {
        return lowerBound;
    }

    public IdentifierExpression getToKeyword() {
        return toKeyword;
    }

    public Expression getUpperBound() {
        return upperBound;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.RANGE_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)lowerBound, toKeyword, upperBound).iterator();
    }
}
