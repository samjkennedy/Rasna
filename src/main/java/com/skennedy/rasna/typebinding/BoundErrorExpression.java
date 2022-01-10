package com.skennedy.rasna.typebinding;

import java.util.Collections;
import java.util.Iterator;

public class BoundErrorExpression implements BoundExpression {

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.ERROR_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return TypeSymbol.ERROR;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.emptyIterator();
    }
}
