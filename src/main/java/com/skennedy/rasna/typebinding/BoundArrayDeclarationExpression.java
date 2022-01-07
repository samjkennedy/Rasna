package com.skennedy.rasna.typebinding;

import java.util.Collections;
import java.util.Iterator;

public class BoundArrayDeclarationExpression implements BoundExpression {

    private final ArrayTypeSymbol type;
    private final BoundExpression elementCount;

    public BoundArrayDeclarationExpression(ArrayTypeSymbol type, BoundExpression elementCount) {
        this.type = type;
        this.elementCount = elementCount;
    }

    public BoundExpression getElementCount() {
        return elementCount;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.ARRAY_DECLARATION_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return type;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.singleton(elementCount).iterator();
    }
}
