package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class ReturnExpression extends Expression {

    private final IdentifierExpression returnKeyword;
    private final Expression returnValue;

    public ReturnExpression(IdentifierExpression returnKeyword, Expression returnValue) {

        this.returnKeyword = returnKeyword;
        this.returnValue = returnValue;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.RETURN_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)returnKeyword, returnValue).iterator();
    }

    public IdentifierExpression getReturnKeyword() {
        return returnKeyword;
    }

    public Expression getReturnValue() {
        return returnValue;
    }
}
