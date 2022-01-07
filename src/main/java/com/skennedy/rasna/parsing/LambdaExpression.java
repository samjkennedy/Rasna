package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LambdaExpression extends Expression {

    private final List<FunctionArgumentExpression> argumentExpressions;
    private final IdentifierExpression arrow;
    private final Expression expression;

    public LambdaExpression(List<FunctionArgumentExpression> argumentExpressions, IdentifierExpression arrow, Expression expression) {
        this.argumentExpressions = argumentExpressions;
        this.arrow = arrow;
        this.expression = expression;
    }

    public List<FunctionArgumentExpression> getArgumentExpressions() {
        return argumentExpressions;
    }

    public IdentifierExpression getArrow() {
        return arrow;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.LAMBDA_EXPRESSION;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        List<SyntaxNode> children = new ArrayList<>(argumentExpressions);
        children.add(arrow);
        children.add(expression);
        return children.iterator();
    }
}
