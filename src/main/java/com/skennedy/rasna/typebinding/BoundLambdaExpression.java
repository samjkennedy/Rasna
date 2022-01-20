package com.skennedy.rasna.typebinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BoundLambdaExpression implements BoundExpression {

    private final List<BoundFunctionParameterExpression> arguments;
    private final BoundExpression body;

    public BoundLambdaExpression(List<BoundFunctionParameterExpression> arguments, BoundExpression body) {
        this.arguments = arguments;
        this.body = body;
    }

    public List<BoundFunctionParameterExpression> getArguments() {
        return arguments;
    }

    public BoundExpression getBody() {
        return body;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.LAMBDA_FUNCTION;
    }

    @Override
    public TypeSymbol getType() {
        return TypeSymbol.FUNCTION;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        List<BoundExpression> children = new ArrayList<>(arguments);
        children.add(body);
        return children.iterator();
    }
}
