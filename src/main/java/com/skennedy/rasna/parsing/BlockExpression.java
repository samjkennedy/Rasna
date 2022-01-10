package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class BlockExpression extends Expression {

    private final IdentifierExpression openCurly;
    private final List<Expression> expressions;
    private final IdentifierExpression closeCurly;

    public BlockExpression(IdentifierExpression openCurly, List<Expression> expressions, IdentifierExpression closeCurly) {
        this.openCurly = openCurly;
        this.expressions = expressions;
        this.closeCurly = closeCurly;
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
        List<SyntaxNode> children = new ArrayList<>();
        children.add(openCurly);
        children.addAll(expressions.stream()
                .map(node -> (SyntaxNode)node)
                .collect(Collectors.toList()));
        children.add(closeCurly);
        return children.iterator();
    }
}
