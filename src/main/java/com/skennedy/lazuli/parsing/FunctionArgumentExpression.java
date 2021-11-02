package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class FunctionArgumentExpression extends Expression {

    private final IdentifierExpression constKeyword;
    private final IdentifierExpression typeKeyword;
    private final IdentifierExpression identifier;
    private final IdentifierExpression bar;
    private final Expression range;

    public FunctionArgumentExpression(IdentifierExpression constKeyword, IdentifierExpression typeKeyword, IdentifierExpression identifier, IdentifierExpression bar, Expression range) {
        this.constKeyword = constKeyword;
        this.typeKeyword = typeKeyword;
        this.identifier = identifier;
        this.bar = bar;
        this.range = range;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.FUNC_ARG_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)typeKeyword, identifier, bar, range).iterator();
    }

    public IdentifierExpression getConstKeyword() {
        return constKeyword;
    }

    public IdentifierExpression getTypeKeyword() {
        return typeKeyword;
    }

    public IdentifierExpression getIdentifier() {
        return identifier;
    }

    public IdentifierExpression getBar() {
        return bar;
    }

    public Expression getRange() {
        return range;
    }
}
