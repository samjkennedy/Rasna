package com.skennedy.rasna.typebinding;

import java.util.Collections;
import java.util.Iterator;

public class BoundFunctionParameterExpression implements BoundExpression {

    private final boolean reference;
    private final VariableSymbol argument;
    private final BoundExpression guard;

    public BoundFunctionParameterExpression(boolean reference, VariableSymbol argument, BoundExpression guard) {
        this.reference = reference;

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

    public boolean isReference() {
        return reference;
    }

    public VariableSymbol getArgument() {
        return argument;
    }

    public BoundExpression getGuard() {
        return guard;
    }

    String getSignature() {
        StringBuilder sb = new StringBuilder();
        if (isReference()) {
            sb.append("ref ");
        }
        if (argument.isReadOnly()) {
            sb.append("const ");
        }
        sb.append(getType());

        return sb.toString();
    }
}
