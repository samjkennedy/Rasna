package com.skennedy.rasna.typebinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BoundMatchExpression implements BoundExpression {

    private final TypeSymbol type;
    private final BoundExpression operand;
    private final List<BoundMatchCaseExpression> matchCaseExpressions;

    public BoundMatchExpression(TypeSymbol type, BoundExpression operand, List<BoundMatchCaseExpression> matchCaseExpressions) {
        this.type = type;
        this.operand = operand;
        this.matchCaseExpressions = matchCaseExpressions;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.MATCH_EXPRESSION;
    }

    @Override
    public TypeSymbol getType() {
        return type;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        List<BoundExpression> children = new ArrayList<>();
        children.add(operand);
        children.addAll(matchCaseExpressions);
        return children.iterator();
    }

    public BoundExpression getOperand() {
        return operand;
    }

    public List<BoundMatchCaseExpression> getMatchCaseExpressions() {
        return matchCaseExpressions;
    }
}
