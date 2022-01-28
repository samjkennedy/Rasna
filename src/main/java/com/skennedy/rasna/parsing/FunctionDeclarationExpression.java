package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class FunctionDeclarationExpression extends Expression {

    private final IdentifierExpression fnKeyword;
    private List<Expression> genericParameters;
    private final IdentifierExpression identifier;
    private final IdentifierExpression openParen;
    private final List<FunctionParameterExpression> arguments;
    private final IdentifierExpression closeParen;
    private final TypeExpression typeExpression;
    private final BlockExpression body;

    public FunctionDeclarationExpression(IdentifierExpression fnKeyword, List<Expression> genericParameters, IdentifierExpression identifier, IdentifierExpression openParen, List<FunctionParameterExpression> argumentExpressions, IdentifierExpression closeParen, TypeExpression typeExpression, BlockExpression body) {

        this.fnKeyword = fnKeyword;
        this.genericParameters = genericParameters;
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

        List<SyntaxNode> children = new ArrayList<>();
        children.add(fnKeyword);
        children.addAll(genericParameters);
        children.add(openParen);
        children.add(closeParen);
        children.add(typeExpression);
        children.add(body);

        return children.iterator();
    }

    public IdentifierExpression getFnKeyword() {
        return fnKeyword;
    }

    public List<Expression> getGenericParameters() {
        return genericParameters;
    }

    public IdentifierExpression getIdentifier() {
        return identifier;
    }

    public IdentifierExpression getOpenParen() {
        return openParen;
    }

    public List<FunctionParameterExpression> getArguments() {
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
