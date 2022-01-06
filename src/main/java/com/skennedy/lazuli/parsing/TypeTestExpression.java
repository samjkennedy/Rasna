package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Collections;
import java.util.Iterator;

public class TypeTestExpression extends Expression {

    private final Expression expression;
    private final IdentifierExpression isKeyword;
    private final IdentifierExpression typeLiteral;

    public TypeTestExpression(Expression expression, IdentifierExpression isKeyword, IdentifierExpression typeLiteral) {
        this.expression = expression;
        this.isKeyword = isKeyword;
        this.typeLiteral = typeLiteral;
    }

    public Expression getExpression() {
        return expression;
    }

    public IdentifierExpression getIsKeyword() {
        return isKeyword;
    }

    public IdentifierExpression getTypeLiteral() {
        return typeLiteral;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.TYPE_TEST_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Collections.singleton((SyntaxNode)typeLiteral).iterator();
    }
}
