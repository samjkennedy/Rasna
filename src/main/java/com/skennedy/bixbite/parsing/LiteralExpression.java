package com.skennedy.bixbite.parsing;

import com.skennedy.bixbite.parsing.model.ExpressionType;
import com.skennedy.bixbite.parsing.model.SyntaxNode;

import java.util.Collections;
import java.util.Iterator;

public class LiteralExpression extends Expression {

    private final Object value;

    public LiteralExpression(Object value) {
        this.value = value;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.LITERAL_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Collections.emptyIterator();
    }

    public Object getValue() {
        return value;
    }
}
