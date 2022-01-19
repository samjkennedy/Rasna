package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StructLiteralExpression extends Expression {

    private final TypeExpression typeExpression;
    private final IdentifierExpression openCurly;
    private final List<Expression> members;
    private final IdentifierExpression closeCurly;

    public StructLiteralExpression(TypeExpression typeExpression, IdentifierExpression openCurly, List<Expression> members, IdentifierExpression closeCurly) {
        this.typeExpression = typeExpression;
        this.openCurly = openCurly;
        this.members = members;
        this.closeCurly = closeCurly;
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
        children.add(openCurly);
        children.addAll(members);
        children.add(closeCurly);
        return children.iterator();
    }
}
