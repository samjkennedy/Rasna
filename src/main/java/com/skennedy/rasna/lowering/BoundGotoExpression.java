package com.skennedy.rasna.lowering;

import com.skennedy.rasna.typebinding.BoundExpression;
import com.skennedy.rasna.typebinding.BoundExpressionType;
import com.skennedy.rasna.typebinding.TypeSymbol;

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
        return TypeSymbol.UNIT;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.emptyIterator();
    }

    public BoundLabel getLabel() {
        return label;
    }
}
