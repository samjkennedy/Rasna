package com.skennedy.rasna.typebinding;

public class BoundArrayVariableDeclarationExpression extends BoundVariableDeclarationExpression {

    private final int elementCount;

    public BoundArrayVariableDeclarationExpression(VariableSymbol variable, BoundExpression guard, BoundExpression initialiser, int elementCount, boolean readOnly) {
        super(variable, guard, initialiser, readOnly);
        this.elementCount = elementCount;
    }

    public int getElementCount() {
        return elementCount;
    }
}
