package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.OpType;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Collections;
import java.util.Iterator;

public class UnaryExpression extends Expression {

    private final OpType operator;
    private final Expression operand;

    public UnaryExpression(OpType operator, Expression operand) {
        this.operator = operator;
        this.operand = operand;
    }

    public OpType getOperator() {
        return operator;
    }

    public Expression getOperand() {
        return operand;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.UNARY_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Collections.singletonList((SyntaxNode) operand).iterator();
    }
}
