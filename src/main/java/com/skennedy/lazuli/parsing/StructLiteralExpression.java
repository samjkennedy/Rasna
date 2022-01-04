package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StructLiteralExpression extends Expression {

    private TypeExpression typeExpression;
    private List<Expression> members;

    public StructLiteralExpression(TypeExpression typeExpression, List<Expression> members) {
        this.typeExpression = typeExpression;
        this.members = members;
    }

    public TypeExpression getTypeExpression() {
        return typeExpression;
    }

    public List<Expression> getMembers() {
        return members;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.STRUCT_LITERAL_EXPRESSION;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        List<SyntaxNode> children = new ArrayList<>();
        children.add(typeExpression);
        children.addAll(members);
        return children.iterator();
    }
}
