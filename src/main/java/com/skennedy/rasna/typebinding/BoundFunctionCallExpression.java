package com.skennedy.rasna.typebinding;

import java.util.Iterator;
import java.util.List;

public class BoundFunctionCallExpression implements BoundExpression {

    private final FunctionSymbol function;
    private final List<BoundExpression> boundArguments;

    public BoundFunctionCallExpression(FunctionSymbol function, List<BoundExpression> boundArguments) {
        this.function = function;
        this.boundArguments = boundArguments;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.FUNCTION_CALL;
    }

    @Override
    public TypeSymbol getType() {
        return function.getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return boundArguments.iterator();
    }

    public FunctionSymbol getFunction() {
        return function;
    }

    public List<BoundExpression> getBoundArguments() {
        return boundArguments;
    }
}
