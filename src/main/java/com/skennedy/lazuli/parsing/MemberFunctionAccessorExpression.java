package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MemberFunctionAccessorExpression extends MemberAccessorExpression {

    private final IdentifierExpression openParen;
    private final List<Expression> arguments;
    private final IdentifierExpression closeParen;

    public MemberFunctionAccessorExpression(IdentifierExpression owner, IdentifierExpression dot, IdentifierExpression member, IdentifierExpression openParen, List<Expression> arguments, IdentifierExpression closeParen) {
        super(owner, dot, member);
        this.openParen = openParen;
        this.arguments = arguments;
        this.closeParen = closeParen;
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

    @Override
    public Iterator<SyntaxNode> getChildren() {

        List<SyntaxNode> children = new ArrayList<>();
        children.addAll(Arrays.asList(owner, dot, member, openParen));
        children.addAll(arguments);
        children.add(closeParen);

        return children.iterator();
    }
}
