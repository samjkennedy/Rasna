package com.skennedy.rasna.lowering;

import com.skennedy.rasna.typebinding.BoundExpression;
import com.skennedy.rasna.typebinding.BoundExpressionType;
import com.skennedy.rasna.typebinding.TypeSymbol;

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
