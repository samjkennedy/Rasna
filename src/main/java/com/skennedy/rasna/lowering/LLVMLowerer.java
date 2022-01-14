package com.skennedy.rasna.lowering;

import com.skennedy.rasna.parsing.model.OpType;
import com.skennedy.rasna.typebinding.*;

import java.util.Iterator;

public class LLVMLowerer extends BoundProgramRewriter {

    @Override
    protected BoundExpression rewriteForExpression(BoundForExpression boundForExpression) {

        BoundExpression expression = super.rewriteForExpression(boundForExpression);

        if (!(expression instanceof BoundForExpression)) {
            return expression;
        }

        BoundForExpression rewrittenForExpression = (BoundForExpression) expression;

        if (rewrittenForExpression.getBody() instanceof BoundNoOpExpression) {
            return new BoundNoOpExpression();
        }

        BoundVariableDeclarationExpression variableDeclarationExpression = new BoundVariableDeclarationExpression(rewrittenForExpression.getIterator(), null, rewrittenForExpression.getRangeExpression().getLowerBound(), false);

        BoundVariableExpression variableExpression = new BoundVariableExpression(rewrittenForExpression.getIterator());

        BoundExpression step;
        if (rewrittenForExpression.getRangeExpression().getStep() == null) {
            step = new BoundAssignmentExpression(rewrittenForExpression.getIterator(),
                    null,
                    new BoundBinaryExpression(
                            variableExpression,
                            BoundBinaryOperator.bind(OpType.ADD, TypeSymbol.INT, TypeSymbol.INT),
                            new BoundLiteralExpression(1)));
        } else {
            step = new BoundAssignmentExpression(rewrittenForExpression.getIterator(),
                    null,
                    new BoundBinaryExpression(
                            variableExpression,
                            BoundBinaryOperator.bind(OpType.ADD, TypeSymbol.INT, TypeSymbol.INT),
                            rewrittenForExpression.getRangeExpression().getStep()));
        }

        BoundBlockExpression whileBody;
        if (rewrittenForExpression.getGuard() == null) {
            whileBody = new BoundBlockExpression(rewrittenForExpression.getBody(), step);
        } else {
            BoundExpression guardClause = new BoundIfExpression(rewrittenForExpression.getGuard(), rewrittenForExpression.getBody(), null);
            whileBody = new BoundBlockExpression(guardClause, step);
        }

        BoundBinaryExpression condition = new BoundBinaryExpression(variableExpression, BoundBinaryOperator.bind(OpType.LT, TypeSymbol.INT, TypeSymbol.INT), boundForExpression.getRangeExpression().getUpperBound());

        BoundExpression whileExpression = rewriteWhileExpression(new BoundWhileExpression(condition, whileBody));
        BoundBlockExpression boundBlockExpression = new BoundBlockExpression(variableDeclarationExpression, whileExpression);

        return rewriteBlockExpression(boundBlockExpression);
    }

    @Override
    protected BoundExpression rewriteWhileExpression(BoundWhileExpression boundWhileExpression) {

        BoundExpression expression = super.rewriteWhileExpression(boundWhileExpression);

        if (!(expression instanceof BoundWhileExpression)) {
            return expression;
        }

        BoundDoWhileExpression doWhileCondition = new BoundDoWhileExpression(boundWhileExpression.getBody(), boundWhileExpression.getCondition());

        return rewriteExpression(new BoundIfExpression(boundWhileExpression.getCondition(), doWhileCondition, null));
    }

    @Override
    protected BoundExpression rewriteMatchExpression(BoundMatchExpression matchExpression) {

        BoundExpression expression = super.rewriteMatchExpression(matchExpression);

        if (expression instanceof BoundNoOpExpression) {
            return expression;
        }
        BoundMatchExpression rewrittenMatchExpression = (BoundMatchExpression) expression;

        Iterator<BoundMatchCaseExpression> iterator = rewrittenMatchExpression.getMatchCaseExpressions().iterator();
        return rewriteExpression(rewriteMatchCaseExpression(iterator, iterator.next(), matchExpression.getOperand()));
    }

    private BoundExpression rewriteMatchCaseExpression(Iterator<BoundMatchCaseExpression> iterator, BoundMatchCaseExpression matchCaseExpression, BoundExpression operand) {

        if (matchCaseExpression.getCaseExpression() == null) { //base case
            return matchCaseExpression.getThenExpression();
        }

        BoundExpression condition;
        if (matchCaseExpression.getCaseExpression().getType() == TypeSymbol.BOOL) {
            condition = matchCaseExpression.getCaseExpression();
        } else {
            condition = new BoundBinaryExpression(operand, BoundBinaryOperator.bind(OpType.EQ, TypeSymbol.INT, TypeSymbol.INT), matchCaseExpression.getCaseExpression());
        }

        if (iterator.hasNext()) {
            return new BoundIfExpression(condition, matchCaseExpression.getThenExpression(), rewriteMatchCaseExpression(iterator, iterator.next(), operand));
        }
        return new BoundIfExpression(condition, matchCaseExpression.getThenExpression(), null);
    }
}
