package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class TupleIndexExpression extends Expression {

    private final Expression tuple;
    private final IdentifierExpression dot;
    private final LiteralExpression index;

    public TupleIndexExpression(Expression tuple, IdentifierExpression dot, LiteralExpression index) {
        this.tuple = tuple;
        this.dot = dot;
        this.index = index;
    }

    public Expression getTuple() {
        return tuple;
    }

    public IdentifierExpression getDot() {
        return dot;
    }

    public LiteralExpression getIndex() {
        return index;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.TUPLE_INDEX_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)tuple, dot, index).iterator();
    }
}
