package com.skennedy.lazuli.parsing;

import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;
import com.skennedy.lazuli.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MatchCaseExpression extends Expression {

    private final Expression caseExpression;
    private final IdentifierExpression arrow;
    private final Expression thenExpression;
    private final IdentifierExpression comma;

    public MatchCaseExpression(Expression caseExpression, IdentifierExpression arrow, Expression thenExpression, IdentifierExpression comma) {

        this.caseExpression = caseExpression;
        this.arrow = arrow;
        this.thenExpression = thenExpression;
        this.comma = comma;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.MATCH_CASE_EXPRESSION;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        List<SyntaxNode> children = new ArrayList<>();

        children.add(caseExpression);
        children.add(arrow);
        children.add(thenExpression);
        children.add(comma);

        return children.iterator();
    }

    public Expression getCaseExpression() {
        return caseExpression;
    }

    public IdentifierExpression getArrow() {
        return arrow;
    }

    public Expression getThenExpression() {
        return thenExpression;
    }

    public IdentifierExpression getComma() {
        return comma;
    }
}
