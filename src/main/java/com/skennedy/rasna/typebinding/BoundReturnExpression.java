package com.skennedy.rasna.typebinding;

import java.util.Collections;
import java.util.Iterator;

public class BoundReturnExpression implements BoundExpression {
    private BoundExpression returnValue;

    public BoundReturnExpression(BoundExpression returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.RETURN;
    }

    @Override
    public TypeSymbol getType() {
        return returnValue.getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.singleton(returnValue).iterator();
    }

    public BoundExpression getReturnValue() {
        return returnValue;
    }
}
