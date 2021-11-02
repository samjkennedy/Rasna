package com.skennedy.lazuli.typebinding;

import java.util.Arrays;
import java.util.Iterator;

public class BoundMatchCaseExpression implements BoundExpression {

    private final BoundExpression caseExpression;
    private final BoundExpression thenExpression;

    public BoundMatchCaseExpression(BoundExpression caseExpression, BoundExpression thenExpression) {
        this.caseExpression = caseExpression;
        this.thenExpression = thenExpression;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.MATCH_CASE_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return thenExpression.getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Arrays.asList(caseExpression, thenExpression).iterator();
    }

    public BoundExpression getCaseExpression() {
        return caseExpression;
    }

    public BoundExpression getThenExpression() {
        return thenExpression;
    }
}
