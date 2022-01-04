package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class CastExpression extends Expression {

    private final Expression expression;
    private final TypeExpression type;

    public CastExpression(Expression expression, TypeExpression type) {
        this.expression = expression;
        this.type = type;
    }

    public Expression getExpression() {
        return expression;
    }

    public TypeExpression getType() {
        return type;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.CAST_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)expression, type).iterator();
    }
}
