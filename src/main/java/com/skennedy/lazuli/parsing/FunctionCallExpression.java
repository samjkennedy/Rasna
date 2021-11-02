package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class FunctionCallExpression extends Expression {

    private final IdentifierExpression identifier;
    private final IdentifierExpression openParen;
    private final List<Expression> arguments;
    private final IdentifierExpression closeParen;

    public FunctionCallExpression(IdentifierExpression identifier, IdentifierExpression openParen, List<Expression> arguments, IdentifierExpression closeParen) {

        this.identifier = identifier;
        this.openParen = openParen;
        this.arguments = arguments;
        this.closeParen = closeParen;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.FUNC_CALL_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        List<SyntaxNode> children = new ArrayList<>();
        children.add(identifier);
        children.add(openParen);
        children.addAll(arguments);
        children.add(closeParen);

        return children.iterator();
    }

    public IdentifierExpression getIdentifier() {
        return identifier;
    }

    public IdentifierExpression getOpenParen() {
        return openParen;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    public IdentifierExpression getCloseParen() {
        return closeParen;
    }
}
