package com.skennedy.lazuli.typebinding;

import java.util.Arrays;
import java.util.Iterator;

public class BoundMemberAccessorExpression implements BoundExpression {

    private final BoundExpression owner;
    private final BoundExpression member;

    public BoundMemberAccessorExpression(BoundExpression owner, BoundExpression member) {

        this.owner = owner;
        this.member = member;
    }

    public BoundExpression getOwner() {
        return owner;
    }

    public BoundExpression getMember() {
        return member;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.MEMBER_ACCESSOR;
    }

    @Override
    public TypeSymbol getType() {
        return member.getType();
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Arrays.asList(owner, member).iterator();
    }
}
