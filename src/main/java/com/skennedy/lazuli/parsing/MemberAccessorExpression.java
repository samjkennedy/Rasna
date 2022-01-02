package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class MemberAccessorExpression extends Expression {

    private final Expression owner;
    private final IdentifierExpression dot;
    private final IdentifierExpression member;

    public MemberAccessorExpression(Expression owner, IdentifierExpression dot, IdentifierExpression member) {
        this.owner = owner;
        this.dot = dot;
        this.member = member;
    }

    public Expression getOwner() {
        return owner;
    }

    public IdentifierExpression getDot() {
        return dot;
    }

    public IdentifierExpression getMember() {
        return member;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.MEMBER_ACCESSOR_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)owner, dot, member).iterator();
    }
}
