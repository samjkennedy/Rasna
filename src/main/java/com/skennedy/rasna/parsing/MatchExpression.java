package com.skennedy.rasna.parsing;

import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MatchExpression extends Expression {

    private final IdentifierExpression matchKeyword;
    private final Expression operand;
    private final IdentifierExpression openCurly;
    private final List<MatchCaseExpression> caseExpressions;
    private final IdentifierExpression closeCurly;

    public MatchExpression(IdentifierExpression matchKeyword, Expression operand, IdentifierExpression openCurly, List<MatchCaseExpression> caseExpressions, IdentifierExpression closeCurly) {
        this.matchKeyword = matchKeyword;
        this.operand = operand;
        this.openCurly = openCurly;
        this.caseExpressions = caseExpressions;
        this.closeCurly = closeCurly;
    }

    @Override
    public ExpressionType getExpressionType() {
        return ExpressionType.MATCH_EXPRESSION;
    }

    @Override
    public Iterator<SyntaxNode> getChildren() {
        List<SyntaxNode> children = new ArrayList<>();

        children.add(matchKeyword);
        children.add(operand);
        children.add(openCurly);
        children.addAll(caseExpressions);
        children.add(closeCurly);

        return children.iterator();
    }

    public IdentifierExpression getMatchKeyword() {
        return matchKeyword;
    }

    public Expression getOperand() {
        return operand;
    }

    public IdentifierExpression getOpenCurly() {
        return openCurly;
    }

    public List<MatchCaseExpression> getCaseExpressions() {
        return caseExpressions;
    }

    public IdentifierExpression getCloseCurly() {
        return closeCurly;
    }
}
