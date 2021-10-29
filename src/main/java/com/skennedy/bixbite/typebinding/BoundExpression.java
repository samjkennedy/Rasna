package com.skennedy.bixbite.typebinding;

import java.util.Iterator;

public interface BoundExpression {

    BoundExpressionType getBoundExpressionType();
    TypeSymbol getType();
    Iterator<BoundExpression> getChildren();

}
