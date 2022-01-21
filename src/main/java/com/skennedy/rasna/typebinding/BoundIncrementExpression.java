package com.skennedy.rasna.typebinding;

import java.util.Collections;
import java.util.Iterator;

/**
 * Impl Note: This acts like the java prefix increment ++i where the increment is evaluated then returned
 */
public class BoundIncrementExpression implements BoundExpression {

    private final VariableSymbol variableSymbol;
    private final BoundLiteralExpression amount;

    public BoundIncrementExpression(VariableSymbol variableSymbol, BoundLiteralExpression amount) {

        this.variableSymbol = variableSymbol;
        this.amount = amount;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.INCREMENT;
    }

    @Override
    public TypeSymbol getType() {
        return variableSymbol.getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.emptyIterator();
    }

    public VariableSymbol getVariableSymbol() {
        return variableSymbol;
    }

    public BoundLiteralExpression getAmount() {
        return amount;
    }
}
