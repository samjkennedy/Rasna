package com.skennedy.lazuli.typebinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BoundLambdaExpression implements BoundExpression {

    private final List<BoundFunctionArgumentExpression> arguments;
    private final BoundExpression body;

    public BoundLambdaExpression(List<BoundFunctionArgumentExpression> arguments, BoundExpression body) {
        this.arguments = arguments;
        this.body = body;
    }

    public List<BoundFunctionArgumentExpression> getArguments() {
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
