package com.skennedy.lazuli.lowering;

import com.skennedy.lazuli.typebinding.BoundExpression;
import com.skennedy.lazuli.typebinding.BoundExpressionType;
import com.skennedy.lazuli.typebinding.TypeSymbol;

import java.util.Collections;
import java.util.Iterator;

public class BoundLabelExpression implements BoundExpression {

    private BoundLabel label;

    public BoundLabelExpression(BoundLabel label) {
        this.label = label;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.LABEL;
    }

    @Override
    public TypeSymbol getType() {
        return TypeSymbol.VOID;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.emptyIterator();
    }

    public BoundLabel getLabel() {
        return label;
    }
}
