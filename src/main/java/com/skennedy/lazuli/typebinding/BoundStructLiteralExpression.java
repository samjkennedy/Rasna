package com.skennedy.lazuli.typebinding;

import java.util.Iterator;
import java.util.List;

//TODO: Allow these to be made literally e.g. {1, 2}
public class BoundStructLiteralExpression implements BoundExpression {

    private List<BoundExpression> elements;

    public BoundStructLiteralExpression(List<BoundExpression> elements) {
        this.elements = elements;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.STRUCT_LITERAL_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return new ArrayTypeSymbol(TypeSymbol.VAR);
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return elements.iterator();
    }

    public List<BoundExpression> getElements() {
        return elements;
    }
}
