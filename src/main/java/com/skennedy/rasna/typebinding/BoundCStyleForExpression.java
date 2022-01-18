package com.skennedy.rasna.typebinding;

import java.util.Arrays;
import java.util.Iterator;

//This is a c style for loop for ease of converting to llvm instructions
public class BoundCStyleForExpression implements BoundExpression {

    private final BoundExpression initialisation;
    private final BoundExpression condition;
    private final BoundExpression postStep;
    private final BoundExpression body;

    public BoundCStyleForExpression(BoundExpression initialisation, BoundExpression condition, BoundExpression postStep, BoundExpression body) {
        this.initialisation = initialisation;
        this.condition = condition;
        this.postStep = postStep;
        this.body = body;
    }

    public BoundExpression getInitialisation() {
        return initialisation;
    }

    public BoundExpression getCondition() {
        return condition;
    }

    public BoundExpression getPostStep() {
        return postStep;
    }

    public BoundExpression getBody() {
        return body;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {

        return BoundExpressionType.C_STYLE_FOR_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return null;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Arrays.asList(condition, postStep, body).iterator();
    }
}
