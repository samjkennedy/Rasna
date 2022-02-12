package com.skennedy.rasna.typebinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BoundWithBlockExpression implements BoundExpression {

    private final BoundVariableDeclarationExpression resource;
    private final BoundBlockExpression body;
    private final BoundFunctionCallExpression closeCall;

    public BoundWithBlockExpression(BoundVariableDeclarationExpression resource, BoundBlockExpression body, BoundFunctionCallExpression closeCall) {
        this.resource = resource;
        this.body = body;
        this.closeCall = closeCall;
    }

    public BoundVariableDeclarationExpression getResource() {
        return resource;
    }

    public BoundBlockExpression getBody() {
        return body;
    }

    public BoundFunctionCallExpression getCloseCall() {
        return closeCall;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.WITH_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return TypeSymbol.UNIT;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        List<BoundExpression> children = new ArrayList<>();

        children.add(resource);
        children.addAll(body.getExpressions());

        return children.iterator();
    }
}
