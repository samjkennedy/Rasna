package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class MemberAccessorExpression extends Expression {

    private final Expression owner;
    private final IdentifierExpression accessor;
    private final Expression member;

    public MemberAccessorExpression(Expression owner, IdentifierExpression accessor, Expression member) {
        this.owner = owner;
        this.accessor = accessor;
        this.member = member;
    }

    public Expression getOwner() {
        return owner;
    }

    public IdentifierExpression getAccessor() {
        return accessor;
    }

    public Expression getMember() {
        return member;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.MEMBER_ACCESSOR_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)owner, accessor, member).iterator();
    }
}
