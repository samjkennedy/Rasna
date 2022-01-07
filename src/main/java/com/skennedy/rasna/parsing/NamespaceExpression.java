package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class NamespaceExpression extends Expression {

    private final IdentifierExpression namespaceKeyword;
    private final IdentifierExpression namespace;
    private final BlockExpression body;
    private boolean inline;

    public NamespaceExpression(IdentifierExpression namespaceKeyword, IdentifierExpression namespace, BlockExpression body, boolean inline) {
        this.namespaceKeyword = namespaceKeyword;
        this.namespace = namespace;
        this.body = body;
        this.inline = inline;
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

    public boolean isInline() {
        return inline;
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
