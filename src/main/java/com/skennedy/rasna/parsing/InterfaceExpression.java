package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InterfaceExpression extends Expression {

    private final IdentifierExpression interfaceKeyword;
    private final IdentifierExpression identifier;
    private final IdentifierExpression openCurly;
    private final List<FunctionSignatureExpression> signatureExpressions;
    private final IdentifierExpression closeCurly;

    public InterfaceExpression(IdentifierExpression interfaceKeyword, IdentifierExpression identifier, IdentifierExpression openCurly, List<FunctionSignatureExpression> signatureExpressions, IdentifierExpression closeCurly) {
        this.interfaceKeyword = interfaceKeyword;
        this.identifier = identifier;
        this.openCurly = openCurly;
        this.signatureExpressions = signatureExpressions;
        this.closeCurly = closeCurly;
    }

    public IdentifierExpression getInterfaceKeyword() {
        return interfaceKeyword;
    }

    public IdentifierExpression getIdentifier() {
        return identifier;
    }

    public IdentifierExpression getOpenCurly() {
        return openCurly;
    }

    public List<FunctionSignatureExpression> getSignatureExpressions() {
        return signatureExpressions;
    }

    public IdentifierExpression getCloseCurly() {
        return closeCurly;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.INTERFACE_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        List<SyntaxNode> children = new ArrayList<>();
        children.add(interfaceKeyword);
        children.add(identifier);
        children.add(openCurly);
        children.addAll(signatureExpressions);
        children.add(closeCurly);

        return children.iterator();
    }
}
