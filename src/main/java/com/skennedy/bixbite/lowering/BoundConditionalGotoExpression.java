package com.skennedy.bixbite.lowering;

import com.skennedy.bixbite.typebinding.BoundExpression;
import com.skennedy.bixbite.typebinding.BoundExpressionType;
import com.skennedy.bixbite.typebinding.TypeSymbol;

import java.util.Collections;
import java.util.Iterator;

public class BoundConditionalGotoExpression implements BoundExpression {

    private final BoundLabel label;
    private final BoundExpression condition;
    private final boolean jumpIfFalse;

    public BoundConditionalGotoExpression(BoundLabel label, BoundExpression condition, boolean jumpIfFalse) {
        this.label = label;
        this.condition = condition;
        this.jumpIfFalse = jumpIfFalse;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.CONDITIONAL_GOTO;
    }

    @Override
    public TypeSymbol getType() {
        return TypeSymbol.VOID;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.singleton(condition).iterator();
    }

    public BoundLabel getLabel() {
        return label;
    }

    public BoundExpression getCondition() {
        return condition;
    }

    public boolean jumpIfFalse() {
        return jumpIfFalse;
    }
}
