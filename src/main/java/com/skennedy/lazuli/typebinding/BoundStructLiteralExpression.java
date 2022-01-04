package com.skennedy.lazuli.typebinding;

import java.util.Iterator;
import java.util.List;

//TODO: Allow these to be made literally e.g. {1, 2}
public class BoundStructLiteralExpression implements BoundExpression {

    private TypeSymbol type;
    private List<BoundExpression> elements;

    public BoundStructLiteralExpression(TypeSymbol type, List<BoundExpression> elements) {
        this.type = type;
        this.elements = elements;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.STRUCT_LITERAL_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return type;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return elements.iterator();
    }

    public List<BoundExpression> getElements() {
        return elements;
    }
}
