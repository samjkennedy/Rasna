package com.skennedy.rasna.typebinding;

import java.util.Collections;
import java.util.Iterator;

public class BoundFunctionArgumentExpression implements BoundExpression {

    private final VariableSymbol argument;
    private final BoundExpression guard;

    public BoundFunctionArgumentExpression(VariableSymbol argument, BoundExpression guard) {

        this.argument = argument;
        this.guard = guard;
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
        return Collections.singleton(guard).iterator();
    }

    public VariableSymbol getArgument() {
        return argument;
    }

    public BoundExpression getGuard() {
        return guard;
    }
}
