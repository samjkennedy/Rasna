package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Collections;
import java.util.Iterator;

public class TypeExpression extends Expression {

    final Expression type;

    public TypeExpression(Expression type) {
        this.type = type;
    }

    public Expression getTypeExpression() {
        return type;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.TYPE_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Collections.singleton((SyntaxNode)type).iterator();
    }
}
