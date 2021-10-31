package com.skennedy.lazuli.typebinding;

public class BoundConstDeclarationExpression extends BoundVariableExpression {

    private final BoundLiteralExpression constValue;

    public BoundConstDeclarationExpression(VariableSymbol variableSymbol, BoundLiteralExpression constValue) {
        super(variableSymbol);
        this.constValue = constValue;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.VARIABLE_DECLARATION;
    }

    public BoundLiteralExpression getConstValue() {
        return constValue;
    }
}
