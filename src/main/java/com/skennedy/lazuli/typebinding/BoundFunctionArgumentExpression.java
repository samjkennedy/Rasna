package com.skennedy.lazuli.typebinding;

import java.util.Collections;
import java.util.Iterator;

public class BoundFunctionArgumentExpression implements BoundExpression {

    private final VariableSymbol argument;
    private final BoundExpression range;

    public BoundFunctionArgumentExpression(VariableSymbol argument, BoundExpression range) {

        this.argument = argument;
        this.range = range;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.FUNCTION_ARGUMENT;
    }

    @Override
    public TypeSymbol getType() {
        return argument.getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.singleton(range).iterator();
    }

    public VariableSymbol getArgument() {
        return argument;
    }

    public BoundExpression getRange() {
        return range;
    }
}
