package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class VariableDeclarationExpression extends Expression {

    private final IdentifierExpression constKeyword;
    private final TypeExpression typeExpression;
    private IdentifierExpression colon;
    private final IdentifierExpression identifier;
    private final IdentifierExpression bar;
    private final Expression guard;
    private final IdentifierExpression equals;
    private final Expression initialiser;

    public VariableDeclarationExpression(IdentifierExpression constKeyword, TypeExpression typeExpression, IdentifierExpression colon, IdentifierExpression identifier, IdentifierExpression bar, Expression guard, IdentifierExpression equals, Expression initialiser) {
        this.constKeyword = constKeyword;
        this.typeExpression = typeExpression;
        this.colon = colon;
        this.identifier = identifier;
        this.bar = bar;
        this.guard = guard;
        this.equals = equals;
        this.initialiser = initialiser;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.VAR_DECLARATION_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode) constKeyword, typeExpression, colon, identifier, bar, guard, equals, initialiser).iterator();
    }

    public IdentifierExpression getConstKeyword() {
        return constKeyword;
    }

    public TypeExpression getTypeExpression() {
        return typeExpression;
    }

    public IdentifierExpression getColon() {
        return colon;
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

    public IdentifierExpression getEquals() {
        return equals;
    }

    public Expression getInitialiser() {
        return initialiser;
    }
}
