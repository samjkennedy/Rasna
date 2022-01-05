package com.skennedy.lazuli.typebinding;

public class BoundConstDeclarationExpression extends BoundVariableDeclarationExpression {

    private final BoundLiteralExpression constValue;

    public BoundConstDeclarationExpression(VariableSymbol variableSymbol, BoundExpression guard, BoundExpression initialiser, BoundLiteralExpression constValue) {
        super(variableSymbol, guard, initialiser, true);
        this.constValue = constValue;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.CONST_DECLARATION;
    }

    public BoundLiteralExpression getConstValue() {
        return constValue;
    }
}
