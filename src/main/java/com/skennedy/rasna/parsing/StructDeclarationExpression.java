package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class StructDeclarationExpression extends Expression {

    private final IdentifierExpression structKeyword;
    private final TypeExpression typeDefinition;
    private final IdentifierExpression openCurly;
    private final List<Expression> members;
    private final IdentifierExpression closeCurly;

    public StructDeclarationExpression(IdentifierExpression structKeyword, TypeExpression typeDefinition, IdentifierExpression openCurly, List<Expression> members, IdentifierExpression closeCurly) {
        this.structKeyword = structKeyword;
        this.typeDefinition = typeDefinition;
        this.openCurly = openCurly;
        this.members = members;
        this.closeCurly = closeCurly;
    }

    public IdentifierExpression getStructKeyword() {
        return structKeyword;
    }

    public TypeExpression getTypeDefinition() {
        return typeDefinition;
    }

    public IdentifierExpression getOpenCurly() {
        return openCurly;
    }

    public List<Expression> getMembers() {
        return members;
    }

    public IdentifierExpression getCloseCurly() {
        return closeCurly;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.STRUCT_DECLARATION_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        List<SyntaxNode> children = new ArrayList<>();

        children.addAll(Arrays.asList((SyntaxNode)structKeyword, typeDefinition, openCurly));
        children.addAll(members);
        children.add(closeCurly);

        return children.iterator();
    }
}
