package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EnumDeclarationExpression extends Expression {

    private final IdentifierExpression enumKeyword;
    private IdentifierExpression idendifier;
    private final IdentifierExpression openCurly;
    private final List<IdentifierExpression> members;
    private final IdentifierExpression closeCurly;

    public EnumDeclarationExpression(IdentifierExpression enumKeyword, IdentifierExpression idendifier, IdentifierExpression openCurly, List<IdentifierExpression> members, IdentifierExpression closeCurly) {
        this.enumKeyword = enumKeyword;
        this.idendifier = idendifier;
        this.openCurly = openCurly;
        this.members = members;
        this.closeCurly = closeCurly;
    }

    public IdentifierExpression getEnumKeyword() {
        return enumKeyword;
    }

    public IdentifierExpression getIdendifier() {
        return idendifier;
    }

    public IdentifierExpression getOpenCurly() {
        return openCurly;
    }

    public List<IdentifierExpression> getMembers() {
        return members;
    }

    public IdentifierExpression getCloseCurly() {
        return closeCurly;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.ENUM_DECLARATION_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        List<SyntaxNode> children = new ArrayList<>();
        children.add(enumKeyword);
        children.add(openCurly);
        children.addAll(members);
        children.add(closeCurly);

        return children.iterator();
    }
}
