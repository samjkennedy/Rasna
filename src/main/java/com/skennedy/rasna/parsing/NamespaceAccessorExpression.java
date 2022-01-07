package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.Arrays;
import java.util.Iterator;

public class NamespaceAccessorExpression extends Expression {

    private final IdentifierExpression namespace;
    private final IdentifierExpression namespaceAccessor;
    private final Expression expression;

    public NamespaceAccessorExpression(IdentifierExpression namespace, IdentifierExpression namespaceAccessor, Expression expression) {

        this.namespace = namespace;
        this.namespaceAccessor = namespaceAccessor;
        this.expression = expression;
    }

    public IdentifierExpression getNamespace() {
        return namespace;
    }

    public IdentifierExpression getNamespaceAccessor() {
        return namespaceAccessor;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.NAMESPACE_ACCESSOR_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        return Arrays.asList((SyntaxNode)namespace, namespaceAccessor, expression).iterator();
    }
}
