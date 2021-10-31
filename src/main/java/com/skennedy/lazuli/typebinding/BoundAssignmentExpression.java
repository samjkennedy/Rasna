package com.skennedy.lazuli.typebinding;

import java.util.Collections;
import java.util.Iterator;

public class BoundAssignmentExpression implements BoundExpression {

    private final VariableSymbol variable;
    private BoundExpression range;
    private final BoundExpression expression;

    public BoundAssignmentExpression(VariableSymbol variable, BoundExpression range, BoundExpression expression) {
        this.variable = variable;
        this.range = range;
        this.expression = expression;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.ASSIGNMENT_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return expression.getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.singletonList(expression).iterator();
    }

    public VariableSymbol getVariable() {
        return variable;
    }

    public BoundExpression getRange() {
        return range;
    }

    public BoundExpression getExpression() {
        return expression;
    }
}
