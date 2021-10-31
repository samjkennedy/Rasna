package com.skennedy.lazuli.typebinding;

import java.util.Arrays;
import java.util.Iterator;

public class BoundWhileExpression implements BoundExpression {

    private final BoundExpression condition;
    private final BoundBlockExpression body;

    public BoundWhileExpression(BoundExpression condition, BoundBlockExpression body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.WHILE;
    }

    @Override
    public TypeSymbol getType() {
        return body.getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Arrays.asList(condition, body).iterator();
    }

    public BoundExpression getCondition() {
        return condition;
    }

    public BoundBlockExpression getBody() {
        return body;
    }
}
