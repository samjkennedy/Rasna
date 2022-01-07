package com.skennedy.rasna.typebinding;

import java.util.Iterator;
import java.util.List;

public class BoundFunctionDeclarationExpression implements BoundExpression {

    private final FunctionSymbol functionSymbol;
    private List<BoundFunctionArgumentExpression> arguments;
    private final BoundBlockExpression body;

    public BoundFunctionDeclarationExpression(FunctionSymbol functionSymbol, List<BoundFunctionArgumentExpression> arguments, BoundBlockExpression body) {
        this.functionSymbol = functionSymbol;
        this.arguments = arguments;
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

    public List<BoundFunctionArgumentExpression> getArguments() {
        return arguments;
    }

    public BoundBlockExpression getBody() {
        return body;
    }


}
