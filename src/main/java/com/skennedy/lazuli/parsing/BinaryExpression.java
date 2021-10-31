package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.OpType;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class BinaryExpression extends Expression {

    //<left> <op> <right>;
    private final Expression left;
    private final OpType operation;
    private final Expression right;

    public BinaryExpression(Expression left, OpType operation, Expression right) {
        this.left = left;
        this.operation = operation;
        this.right = right;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.BINARY_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)left, right).iterator();
    }

    public OpType getOperation() {
        return operation;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }
}
