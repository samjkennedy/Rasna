package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class PrintExpression extends Expression {

    private final IdentifierExpression printKeyword;
    private final IdentifierExpression openParanthesis;
    private final Expression expression;
    private final IdentifierExpression closeParenthesis;

    public PrintExpression(IdentifierExpression printKeyword, IdentifierExpression openParanthesis, Expression expression, IdentifierExpression closeParenthesis) {
        this.printKeyword = printKeyword;
        this.openParanthesis = openParanthesis;
        this.expression = expression;
        this.closeParenthesis = closeParenthesis;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.PRINT_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)printKeyword, openParanthesis, expression, closeParenthesis).iterator();
    }

    public IdentifierExpression getPrintKeyword() {
        return printKeyword;
    }

    public IdentifierExpression getOpenParanthesis() {
        return openParanthesis;
    }

    public Expression getExpression() {
        return expression;
    }

    public IdentifierExpression getCloseParenthesis() {
        return closeParenthesis;
    }
}
