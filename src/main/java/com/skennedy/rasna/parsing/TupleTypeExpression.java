package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.skennedy.rasna.parsing.model.ExpressionType.TYPE_EXPR;

public class TupleTypeExpression extends Expression {

    private final IdentifierExpression openParenthesis;
    private List<DelimitedExpression<IdentifierExpression>> typeExpressions;
    private final IdentifierExpression closeParenthesis;

    public TupleTypeExpression(IdentifierExpression openParenthesis, List<DelimitedExpression<IdentifierExpression>> typeExpressions, IdentifierExpression closeParenthesis) {
        this.openParenthesis = openParenthesis;
        this.typeExpressions = typeExpressions;
        this.closeParenthesis = closeParenthesis;
    }

    public IdentifierExpression getOpenParenthesis() {
        return openParenthesis;
    }

    public List<DelimitedExpression<IdentifierExpression>> getTypeExpressions() {
        return typeExpressions;
    }

    public IdentifierExpression getCloseParenthesis() {
        return closeParenthesis;
    }

    @Override
    public ExpressionType getExpressionType() {
        return TYPE_EXPR;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        List<SyntaxNode> children = new ArrayList<>();
        children.add(openParenthesis);
        children.addAll(typeExpressions);
        children.add(closeParenthesis);

        return children.iterator();
    }
}
