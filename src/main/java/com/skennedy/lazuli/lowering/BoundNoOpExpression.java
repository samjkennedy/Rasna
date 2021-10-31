package com.skennedy.lazuli.lowering;

import com.skennedy.lazuli.typebinding.BoundExpression;
import com.skennedy.lazuli.typebinding.BoundExpressionType;
import com.skennedy.lazuli.typebinding.TypeSymbol;

import java.util.Collections;
import java.util.Iterator;

public class BoundNoOpExpression implements BoundExpression {

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.NOOP;
    }

    @Override
    public TypeSymbol getType() {
        return TypeSymbol.VOID;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.emptyIterator();
    }
}
