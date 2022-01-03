package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class NamespaceExpression extends Expression {

    private final IdentifierExpression namespaceKeyword;
    private final IdentifierExpression namespace;
    private final BlockExpression body;

    public NamespaceExpression(IdentifierExpression namespaceKeyword, IdentifierExpression namespace, BlockExpression body) {
        this.namespaceKeyword = namespaceKeyword;
        this.namespace = namespace;
        this.body = body;
    }

    public IdentifierExpression getNamespaceKeyword() {
        return namespaceKeyword;
    }

    public IdentifierExpression getNamespace() {
        return namespace;
    }

    public BlockExpression getBody() {
        return body;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.NAMESPACE;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)namespaceKeyword, namespace, body).iterator();
    }
}
