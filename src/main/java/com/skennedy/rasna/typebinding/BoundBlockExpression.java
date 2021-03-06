package com.skennedy.rasna.typebinding;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class BoundBlockExpression implements BoundExpression {

    private final List<BoundExpression> expressions;

    public BoundBlockExpression(BoundExpression... expressions) {
        this(Arrays.asList(expressions));
    }

    public BoundBlockExpression(List<BoundExpression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.BLOCK;
    }

    @Override
    public TypeSymbol getType() {
        return TypeSymbol.UNIT; //TODO: WHat type is a block?
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return CollectionUtils.emptyIfNull(expressions).iterator();
    }

    public List<BoundExpression> getExpressions() {
        return expressions;
    }
}
