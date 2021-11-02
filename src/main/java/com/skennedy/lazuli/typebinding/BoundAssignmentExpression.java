package com.skennedy.lazuli.typebinding;

import java.util.Collections;
import java.util.Iterator;

public class BoundAssignmentExpression implements BoundExpression {

    private final VariableSymbol variable;
    private BoundExpression guard;
    private final BoundExpression expression;

    public BoundAssignmentExpression(VariableSymbol variable, BoundExpression guard, BoundExpression expression) {
        this.variable = variable;
        this.guard = guard;
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

    public BoundExpression getGuard() {
        return guard;
    }

    public BoundExpression getExpression() {
        return expression;
    }
}
