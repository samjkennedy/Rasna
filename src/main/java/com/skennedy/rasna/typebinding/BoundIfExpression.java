package com.skennedy.rasna.typebinding;

import java.util.Arrays;
import java.util.Iterator;

public class BoundIfExpression implements BoundExpression {

    private final BoundExpression condition;
    private final BoundExpression body;
    private final BoundExpression elseBody;

    public BoundIfExpression(BoundExpression condition, BoundExpression body, BoundExpression elseBody) {
        this.condition = condition;
        this.body = body;
        this.elseBody = elseBody;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.IF;
    }

    @Override
    public TypeSymbol getType() {
        return body.getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Arrays.asList(body, elseBody).iterator();
    }

    public BoundExpression getCondition() {
        return condition;
    }

    public BoundExpression getBody() {
        return body;
    }

    public BoundExpression getElseBody() {
        return elseBody;
    }
}
