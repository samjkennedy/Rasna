package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

public class DelimitedExpression<T extends Expression> extends Expression {

    private final T expression;
    private final IdentifierExpression delimiter;

    public DelimitedExpression(T expression, IdentifierExpression delimiter) {
        this.expression = expression;
        this.delimiter = delimiter;
    }

    public T getExpression() {
        return expression;
    }

    public IdentifierExpression getDelimiter() {
        return delimiter;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.DELIMITED_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Stream.of((SyntaxNode)expression, delimiter)
                .filter(Objects::nonNull)
                .iterator();
    }
}
