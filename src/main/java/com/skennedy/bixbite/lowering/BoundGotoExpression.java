package com.skennedy.bixbite.lowering;

import com.skennedy.bixbite.typebinding.BoundExpression;
import com.skennedy.bixbite.typebinding.BoundExpressionType;
import com.skennedy.bixbite.typebinding.TypeSymbol;

import java.util.Collections;
import java.util.Iterator;

public class BoundGotoExpression implements BoundExpression {

    private BoundLabel label;

    public BoundGotoExpression(BoundLabel label) {
        this.label = label;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.GOTO;
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
