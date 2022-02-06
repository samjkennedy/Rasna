package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.IdentifierExpression;

import java.util.List;

public class TypeParameterExpression extends TypeExpression {

    private final IdentifierExpression colon;
    private final IdentifierExpression openParenthesis;
    private final List<TypeExpression> constraints;
    private final IdentifierExpression closeParenthesis;

    public TypeParameterExpression(IdentifierExpression identifier, IdentifierExpression colon, IdentifierExpression openParenthesis, List<TypeExpression> constraints, IdentifierExpression closeParenthesis) {
        super(identifier);
        this.colon = colon;
        this.openParenthesis = openParenthesis;
        this.constraints = constraints;
        this.closeParenthesis = closeParenthesis;
    }

    public IdentifierExpression getColon() {
        return colon;
    }

    public IdentifierExpression getOpenParenthesis() {
        return openParenthesis;
    }

    public List<TypeExpression> getConstraints() {
        return constraints;
    }

    public IdentifierExpression getCloseParenthesis() {
        return closeParenthesis;
    }
}
