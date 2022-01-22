package com.skennedy.rasna.typebinding;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BoundEnumDeclarationExpression implements BoundExpression {

    private final EnumTypeSymbol type;
    private final List<VariableSymbol> members;

    public BoundEnumDeclarationExpression(EnumTypeSymbol type, List<VariableSymbol> members) {
        this.type = type;
        this.members = members;
    }

    public List<VariableSymbol> getMembers() {
        return members;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.ENUM_DECLARATION_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return type;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.emptyIterator();
    }
}
