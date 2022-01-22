package com.skennedy.rasna.typebinding;

import java.util.Collections;
import java.util.Iterator;

public class BoundTypeExpression implements BoundExpression {

    private TypeSymbol typeSymbol;

    public BoundTypeExpression(TypeSymbol typeSymbol) {
        this.typeSymbol = typeSymbol;
    }

    public TypeSymbol getTypeSymbol() {
        return typeSymbol;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.TYPE_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return TypeSymbol.TYPE;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.emptyIterator();
    }
}
