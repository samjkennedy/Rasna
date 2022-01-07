package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class FunctionDeclarationExpression extends Expression {

    private final IdentifierExpression fnKeyword;
    private final IdentifierExpression identifier;
    private final IdentifierExpression openParen;
    private final List<FunctionArgumentExpression> arguments;
    private final IdentifierExpression closeParen;
    private final TypeExpression typeExpression;
    private final BlockExpression body;

    public FunctionDeclarationExpression(IdentifierExpression fnKeyword, IdentifierExpression identifier, IdentifierExpression openParen, List<FunctionArgumentExpression> argumentExpressions, IdentifierExpression closeParen, TypeExpression typeExpression, BlockExpression body) {

        this.fnKeyword = fnKeyword;
        this.identifier = identifier;
        this.openParen = openParen;
        this.arguments = argumentExpressions;
        this.closeParen = closeParen;
        this.typeExpression = typeExpression;
        this.body = body;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.FUNC_DECLARATION_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)fnKeyword, identifier, openParen, closeParen, typeExpression, body).iterator();
    }

    public IdentifierExpression getFnKeyword() {
        return fnKeyword;
    }

    public IdentifierExpression getIdentifier() {
        return identifier;
    }

    public IdentifierExpression getOpenParen() {
        return openParen;
    }

    public List<FunctionArgumentExpression> getArguments() {
        return arguments;
    }

    public IdentifierExpression getCloseParen() {
        return closeParen;
    }

    public TypeExpression getTypeExpression() {
        return typeExpression;
    }

    public BlockExpression getBody() {
        return body;
    }
}
