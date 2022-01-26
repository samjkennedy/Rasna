package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GenericTypeExpression extends TypeExpression {

    private final IdentifierExpression openAngle;
    private final List<IdentifierExpression> genericParameters;
    private final IdentifierExpression closeAngle;

    public GenericTypeExpression(IdentifierExpression typeIdentifier, IdentifierExpression openAngle, List<IdentifierExpression> genericParameters, IdentifierExpression closeAngle) {
        super(typeIdentifier);
        this.openAngle = openAngle;
        this.genericParameters = genericParameters;
        this.closeAngle = closeAngle;
    }

    public IdentifierExpression getOpenAngle() {
        return openAngle;
    }

    public List<IdentifierExpression> getGenericParameters() {
        return genericParameters;
    }

    public IdentifierExpression getCloseAngle() {
        return closeAngle;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.GENERIC_TYPE_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        List<SyntaxNode> children = new ArrayList<>();

        children.add(type);
        children.addAll(genericParameters);
        children.add(openAngle);
        children.add(closeAngle);

        return children.iterator();
    }
}
