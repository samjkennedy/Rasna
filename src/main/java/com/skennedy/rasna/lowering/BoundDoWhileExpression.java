package com.skennedy.rasna.lowering;

import com.skennedy.rasna.typebinding.BoundExpression;
import com.skennedy.rasna.typebinding.BoundExpressionType;
import com.skennedy.rasna.typebinding.TypeSymbol;

import java.util.Arrays;
import java.util.Iterator;

public class BoundDoWhileExpression implements BoundExpression{

    private final BoundExpression body;
    private final BoundExpression condition;

    public BoundDoWhileExpression(BoundExpression body, BoundExpression condition) {
        this.body = body;
        this.condition = condition;
    }

    public BoundExpression getBody() {
        return body;
    }

    public BoundExpression getCondition() {
        return condition;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.DO_WHILE;
    }

    @Override
    public TypeSymbol getType() {
        return body.getType();
    }

    public Iterator<BoundExpression> getChildren() {
        return Arrays.asList(body, condition).iterator();
    }
}
