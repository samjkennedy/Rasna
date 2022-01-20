package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

public class FunctionCallArgumentExpression extends Expression {

    private final IdentifierExpression refKeyword;
    private final Expression expression;

    public FunctionCallArgumentExpression(IdentifierExpression refKeyword, Expression expression) {
        this.refKeyword = refKeyword;
        this.expression = expression;
    }

    public IdentifierExpression getRefKeyword() {
        return refKeyword;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.FUNC_CALL_PARAM_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Stream.of((SyntaxNode)refKeyword, expression)
                .filter(Objects::nonNull).iterator();
    }
}
