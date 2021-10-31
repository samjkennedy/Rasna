package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class WhileExpression extends Expression {

    private final IdentifierExpression whileKeyword;
    private final IdentifierExpression openParen;
    private final Expression condition;
    private final IdentifierExpression closeParen;
    private final BlockExpression body;

    public WhileExpression(IdentifierExpression whileKeyword, IdentifierExpression openParen, Expression condition, IdentifierExpression closeParen, BlockExpression body) {
        this.whileKeyword = whileKeyword;
        this.openParen = openParen;
        this.condition = condition;
        this.closeParen = closeParen;
        this.body = body;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.WHILE_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)whileKeyword, openParen, condition, closeParen, body).iterator();
    }

    public IdentifierExpression getWhileKeyword() {
        return whileKeyword;
    }

    public IdentifierExpression getOpenParen() {
        return openParen;
    }

    public Expression getCondition() {
        return condition;
    }

    public IdentifierExpression getCloseParen() {
        return closeParen;
    }

    public BlockExpression getBody() {
        return body;
    }
}
