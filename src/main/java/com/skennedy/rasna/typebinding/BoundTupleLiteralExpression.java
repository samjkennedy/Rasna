package com.skennedy.rasna.typebinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BoundTupleLiteralExpression implements BoundExpression {

    private final List<TypeSymbol> types;
    private final List<BoundExpression> elements;

    public BoundTupleLiteralExpression(List<BoundExpression> elements) {
        this.elements = elements;

        types = new ArrayList<>();
        for (BoundExpression el : elements) {
            types.add(el.getType());
        }
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.TUPLE_LITERAL_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return new TupleTypeSymbol(types);
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return elements.iterator();
    }

    public List<BoundExpression> getElements() {
        return elements;
    }
}
