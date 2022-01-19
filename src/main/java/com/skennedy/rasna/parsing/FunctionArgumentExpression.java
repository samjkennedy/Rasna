package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public class FunctionArgumentExpression extends Expression {

    private IdentifierExpression refKeyword;
    private final IdentifierExpression constKeyword;
    private final TypeExpression typeExpression;
    private final IdentifierExpression identifier;
    private final IdentifierExpression bar;
    private final Expression guard;

    public FunctionArgumentExpression(IdentifierExpression refKeyword, IdentifierExpression constKeyword, TypeExpression typeExpression, IdentifierExpression identifier, IdentifierExpression bar, Expression guard) {
        this.refKeyword = refKeyword;
        this.constKeyword = constKeyword;
        this.typeExpression = typeExpression;
        this.identifier = identifier;
        this.bar = bar;
        this.guard = guard;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.FUNC_ARG_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)refKeyword, typeExpression, identifier, bar, guard).stream()
                .filter(Objects::nonNull)
                .iterator();
    }

    public IdentifierExpression getRefKeyword() {
        return refKeyword;
    }

    public IdentifierExpression getConstKeyword() {
        return constKeyword;
    }

    public TypeExpression getTypeExpression() {
        return typeExpression;
    }

    public IdentifierExpression getIdentifier() {
        return identifier;
    }

    public IdentifierExpression getBar() {
        return bar;
    }

    public Expression getGuard() {
        return guard;
    }
}
