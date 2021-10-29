package com.skennedy.bixbite.lowering;

import com.skennedy.bixbite.typebinding.BoundExpression;
import com.skennedy.bixbite.typebinding.BoundExpressionType;
import com.skennedy.bixbite.typebinding.TypeSymbol;

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
