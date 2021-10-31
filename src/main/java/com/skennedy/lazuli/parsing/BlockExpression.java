package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class BlockExpression extends Expression {

    private final List<Expression> expressions;

    public BlockExpression(List<Expression> expressions) {
        this.expressions = expressions;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.BLOCK_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return expressions.stream()
                .map(node -> (SyntaxNode)node)
                .collect(Collectors.toList())
                .iterator();
    }
}
