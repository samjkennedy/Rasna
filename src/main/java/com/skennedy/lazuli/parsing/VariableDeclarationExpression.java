package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class VariableDeclarationExpression extends Expression {

    private final IdentifierExpression constKeyword;
    private final TypeExpression typeExpression;
    private final IdentifierExpression identifier;
    private final IdentifierExpression bar;
    private final Expression guard;
    private final IdentifierExpression equals;
    private final Expression initialiser;

    //TODO: roll const into typeExpression
    public VariableDeclarationExpression(IdentifierExpression constKeyword, TypeExpression typeExpression, IdentifierExpression identifier, IdentifierExpression colon, Expression guard, IdentifierExpression equals, Expression initialiser) {
        this.constKeyword = constKeyword;
        this.typeExpression = typeExpression;
        this.identifier = identifier;
        this.bar = colon;
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
        return Arrays.asList((SyntaxNode) constKeyword, typeExpression, identifier, equals, initialiser).iterator();
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

    public IdentifierExpression getEquals() {
        return equals;
    }

    public Expression getInitialiser() {
        return initialiser;
    }
}
