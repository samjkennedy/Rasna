package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class YieldExpression extends Expression {

    private final IdentifierExpression yieldKeyword;
    private final Expression expression;

    public YieldExpression(IdentifierExpression yieldKeyword, Expression expression) {
        this.yieldKeyword = yieldKeyword;
        this.expression = expression;
    }

    public IdentifierExpression getYieldKeyword() {
        return yieldKeyword;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.YIELD_EXPRESSION;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)yieldKeyword, expression).iterator();
    }
}
