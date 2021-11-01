package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class FunctionDeclarationExpression extends Expression {

    private final IdentifierExpression typeKeyword;
    private final IdentifierExpression identifier;
    private final IdentifierExpression openParen;
    private final IdentifierExpression closeParen;
    private final BlockExpression body;

    public FunctionDeclarationExpression(IdentifierExpression typeKeyword, IdentifierExpression identifier, IdentifierExpression openParen, IdentifierExpression closeParen, BlockExpression body) {

        this.typeKeyword = typeKeyword;
        this.identifier = identifier;
        this.openParen = openParen;
        this.closeParen = closeParen;
        this.body = body;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.FUNC_DECLARATION_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)typeKeyword, identifier, openParen, closeParen, body).iterator();
    }

    public IdentifierExpression getTypeKeyword() {
        return typeKeyword;
    }

    public IdentifierExpression getIdentifier() {
        return identifier;
    }

    public IdentifierExpression getOpenParen() {
        return openParen;
    }

    public IdentifierExpression getCloseParen() {
        return closeParen;
    }

    public BlockExpression getBody() {
        return body;
    }
}
