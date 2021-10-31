package com.skennedy.lazuli.lowering;

import com.skennedy.lazuli.typebinding.BoundExpression;
import com.skennedy.lazuli.typebinding.BoundExpressionType;
import com.skennedy.lazuli.typebinding.TypeSymbol;

import java.util.Collections;
import java.util.Iterator;

public class BoundArrayLengthExpression implements BoundExpression {

    private BoundExpression iterable;

    public BoundArrayLengthExpression(BoundExpression iterable) {
        this.iterable = iterable;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.ARRAY_LENGTH_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return TypeSymbol.INT;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.singleton(iterable).iterator();
    }

    public BoundExpression getIterable() {
        return iterable;
    }
}
