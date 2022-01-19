package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MatchCaseExpression extends Expression {

    private final Expression caseExpression;
    private final IdentifierExpression arrow;
    private final Expression thenExpression;

    public MatchCaseExpression(Expression caseExpression, IdentifierExpression arrow, Expression thenExpression) {

        this.caseExpression = caseExpression;
        this.arrow = arrow;
        this.thenExpression = thenExpression;
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
}
