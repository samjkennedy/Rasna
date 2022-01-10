package com.skennedy.rasna.typebinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public interface BoundExpression {

    BoundExpressionType getBoundExpressionType();
    TypeSymbol getType();
    Iterator<BoundExpression> getChildren();

    default boolean isConstExpression() {
        List<BoundExpression> children = new ArrayList<>();
        getChildren().forEachRemaining(child -> {
            if (child != null) {
                children.add(child);
            }
        });
        //Maybe not quite right
        return children.stream().allMatch(BoundExpression::isConstExpression);
    }

    default Object getConstValue() {
        return null;
    }
}
