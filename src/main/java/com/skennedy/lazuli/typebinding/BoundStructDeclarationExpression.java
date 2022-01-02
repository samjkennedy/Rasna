package com.skennedy.lazuli.typebinding;

import java.util.Iterator;
import java.util.List;

public class BoundStructDeclarationExpression implements BoundExpression {

    private final TypeSymbol type;
    private final List<BoundExpression> members;

    public BoundStructDeclarationExpression(TypeSymbol type, List<BoundExpression> members) {
        this.type = type;
        this.members = members;
    }

    public List<BoundExpression> getMembers() {
        return members;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.STRUCT_DECLARATION_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return type;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return members.iterator();
    }
}
