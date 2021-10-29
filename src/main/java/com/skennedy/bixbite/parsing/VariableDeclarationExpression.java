package com.skennedy.bixbite.parsing;

import com.skennedy.bixbite.parsing.model.ExpressionType;
import com.skennedy.bixbite.parsing.model.IdentifierExpression;
import com.skennedy.bixbite.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class VariableDeclarationExpression extends Expression {

    private final IdentifierExpression constKeyword;
    private final IdentifierExpression declarationKeyword;
    private final IdentifierExpression identifier;
    private final IdentifierExpression colon;
    private final Expression range;
    private final IdentifierExpression equals;
    private final Expression initialiser;

    public VariableDeclarationExpression(IdentifierExpression constKeyword, IdentifierExpression declarationKeyword, IdentifierExpression identifier, IdentifierExpression colon, Expression range, IdentifierExpression equals, Expression initialiser) {
        this.constKeyword = constKeyword;
        this.declarationKeyword = declarationKeyword;
        this.identifier = identifier;
        this.colon = colon;
        this.range = range;
        this.equals = equals;
        this.initialiser = initialiser;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.VAR_DECLARATION_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode) constKeyword, declarationKeyword, identifier, equals, initialiser).iterator();
    }

    public IdentifierExpression getConstKeyword() {
        return constKeyword;
    }

    public IdentifierExpression getDeclarationKeyword() {
        return declarationKeyword;
    }

    public IdentifierExpression getIdentifier() {
        return identifier;
    }

    public IdentifierExpression getColon() {
        return colon;
    }

    public Expression getRange() {
        return range;
    }

    public IdentifierExpression getEquals() {
        return equals;
    }

    public Expression getInitialiser() {
        return initialiser;
    }
}
