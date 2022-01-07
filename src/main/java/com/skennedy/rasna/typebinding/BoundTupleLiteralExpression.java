package com.skennedy.rasna.typebinding;

import java.util.Iterator;
import java.util.List;

public class BoundTupleLiteralExpression implements BoundExpression {

    private List<BoundExpression> elements;

    public BoundTupleLiteralExpression(List<BoundExpression> elements) {
        this.elements = elements;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.TUPLE_LITERAL_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return TypeSymbol.TUPLE;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return elements.iterator();
    }

    public List<BoundExpression> getElements() {
        return elements;
    }
}
