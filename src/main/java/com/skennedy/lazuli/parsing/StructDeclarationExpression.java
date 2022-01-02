package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class StructDeclarationExpression extends Expression {

    private final IdentifierExpression structKeyword;
    private final IdentifierExpression identifier;
    private final IdentifierExpression openCurly;
    private final List<Expression> members;
    private final IdentifierExpression closeCurly;

    public StructDeclarationExpression(IdentifierExpression structKeyword, IdentifierExpression identifier, IdentifierExpression openCurly, List<Expression> members, IdentifierExpression closeCurly) {
        this.structKeyword = structKeyword;
        this.identifier = identifier;
        this.openCurly = openCurly;
        this.members = members;
        this.closeCurly = closeCurly;
    }

    public IdentifierExpression getStructKeyword() {
        return structKeyword;
    }

    public IdentifierExpression getIdentifier() {
        return identifier;
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

        children.addAll(Arrays.asList((SyntaxNode)structKeyword, identifier, openCurly));
        children.addAll(members);
        children.add(closeCurly);

        return children.iterator();
    }
}
