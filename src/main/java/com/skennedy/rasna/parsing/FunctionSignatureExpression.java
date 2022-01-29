package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FunctionSignatureExpression extends Expression {

    private final IdentifierExpression identifier;
    private final IdentifierExpression openParen;
    private final List<FunctionParameterExpression> argumentExpressions;
    private final IdentifierExpression closeParen;
    private final TypeExpression typeExpression;

    public FunctionSignatureExpression(IdentifierExpression identifier, IdentifierExpression openParen, List<FunctionParameterExpression> argumentExpressions, IdentifierExpression closeParen, TypeExpression typeExpression) {

        this.identifier = identifier;
        this.openParen = openParen;
        this.argumentExpressions = argumentExpressions;
        this.closeParen = closeParen;
        this.typeExpression = typeExpression;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.FUNC_SIGNATURE_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        List<SyntaxNode> children = new ArrayList<>();
        children.add(identifier);
        children.add(openParen);
        children.addAll(argumentExpressions);
        children.add(closeParen);
        children.add(typeExpression);

        return children.iterator();
    }
}
