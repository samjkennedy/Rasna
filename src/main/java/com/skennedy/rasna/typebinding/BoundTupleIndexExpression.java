package com.skennedy.rasna.typebinding;

import java.util.Arrays;
import java.util.Iterator;

public class BoundTupleIndexExpression implements BoundExpression {

    private final BoundExpression tuple;
    private final BoundLiteralExpression index;

    public BoundTupleIndexExpression(BoundExpression tuple, BoundLiteralExpression index) {
        this.tuple = tuple;
        this.index = index;
    }

    public BoundExpression getTuple() {
        return tuple;
    }

    public BoundLiteralExpression getIndex() {
        return index;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.TUPLE_INDEX_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return ((TupleTypeSymbol) tuple.getType()).getTypes().get((int)index.getValue());
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Arrays.asList(tuple, index).iterator();
    }
}
