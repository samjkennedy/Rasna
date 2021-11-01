package com.skennedy.lazuli.typebinding;

import java.util.Iterator;

public class BoundFunctionDeclarationExpression implements BoundExpression {

    private final FunctionSymbol functionSymbol;
    private final BoundBlockExpression body;

    public BoundFunctionDeclarationExpression(FunctionSymbol functionSymbol, BoundBlockExpression body) {
        this.functionSymbol = functionSymbol;
        this.body = body;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.FUNCTION_DECLARATION;
    }

    @Override
    public TypeSymbol getType() {
        return TypeSymbol.FUNCTION;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return body.getChildren();
    }

    public FunctionSymbol getFunctionSymbol() {
        return functionSymbol;
    }

    public BoundBlockExpression getBody() {
        return body;
    }


}
