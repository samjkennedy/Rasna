package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ErasedParameterisedTypeExpression extends TypeExpression {

    private final IdentifierExpression openAngle;
    private final List<Expression> erasedParameters;
    private final IdentifierExpression closeAngle;

    public ErasedParameterisedTypeExpression(Expression type, IdentifierExpression openAngle, List<Expression> erasedParameters, IdentifierExpression closeAngle) {
        super(type);
        this.openAngle = openAngle;
        this.erasedParameters = erasedParameters;
        this.closeAngle = closeAngle;
    }

    public IdentifierExpression getOpenAngle() {
        return openAngle;
    }

    public List<Expression> getErasedParameters() {
        return erasedParameters;
    }

    public IdentifierExpression getCloseAngle() {
        return closeAngle;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.ERASED_TYPE_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        List<SyntaxNode> children = new ArrayList<>();

        children.add(type);
        children.addAll(erasedParameters);
        children.add(openAngle);
        children.add(closeAngle);

        return children.iterator();
    }
}
