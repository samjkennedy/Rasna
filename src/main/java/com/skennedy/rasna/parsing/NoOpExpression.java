package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Collections;
import java.util.Iterator;

public class NoOpExpression extends Expression {
    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.NOOP;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Collections.emptyIterator();
    }
}
