package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class WithBlockExpression extends Expression {

    private final IdentifierExpression withKeyword;
    private final IdentifierExpression openParen;
    private final VariableDeclarationExpression resource;
    private final IdentifierExpression closeParen;
    private final BlockExpression body;

    public WithBlockExpression(IdentifierExpression withKeyword, IdentifierExpression openParen, VariableDeclarationExpression resource, IdentifierExpression closeParen, BlockExpression body) {
        this.withKeyword = withKeyword;
        this.openParen = openParen;
        this.resource = resource;
        this.closeParen = closeParen;
        this.body = body;
    }

    public IdentifierExpression getWithKeyword() {
        return withKeyword;
    }

    public IdentifierExpression getOpenParen() {
        return openParen;
    }

    public VariableDeclarationExpression getResource() {
        return resource;
    }

    public IdentifierExpression getCloseParen() {
        return closeParen;
    }

    public BlockExpression getBody() {
        return body;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.WITH_BLOCK_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)withKeyword, openParen, resource, closeParen, body).iterator();
    }
}
