package com.skennedy.rasna.typebinding;

import java.util.Iterator;
import java.util.List;

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
