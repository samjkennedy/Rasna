package com.skennedy.rasna.typebinding;

import java.util.Collections;
import java.util.Iterator;

public class BoundPrintExpression implements BoundExpression {

    private BoundExpression expression;

    public BoundPrintExpression(BoundExpression expression) {
        this.expression = expression;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.PRINT_INTRINSIC;
    }

    @Override
    public TypeSymbol getType() {
        return TypeSymbol.VOID;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.singleton(expression).iterator();
    }

    public BoundExpression getExpression() {
        return expression;
    }
}
