package com.skennedy.lazuli.typebinding;

import java.util.Iterator;
import java.util.List;

public class BoundArrayLiteralExpression implements BoundExpression {

    private List<BoundExpression> elements;

    public BoundArrayLiteralExpression(List<BoundExpression> elements) {
        this.elements = elements;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.ARRAY_LITERAL_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return TypeSymbol.ARRAY; //TODO Array type
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return elements.iterator();
    }

    public List<BoundExpression> getElements() {
        return elements;
    }
}
