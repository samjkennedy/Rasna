package com.skennedy.rasna.lowering;

import com.skennedy.rasna.parsing.model.OpType;
import com.skennedy.rasna.typebinding.*;

import java.util.Iterator;

public class LLVMLowerer extends Lowerer {

    @Override
    protected BoundExpression rewriteVariableDeclaration(BoundVariableDeclarationExpression variableDeclarationExpression) {
        BoundExpression expression = super.rewriteVariableDeclaration(variableDeclarationExpression);

        if (!(expression instanceof BoundVariableDeclarationExpression)) {
            return expression;
        }
        BoundVariableDeclarationExpression rewrittenExpression = (BoundVariableDeclarationExpression) expression;

        if (!(rewrittenExpression.getType() instanceof ArrayTypeSymbol)) {
            return rewrittenExpression;
        }

        BoundExpression initialiser = variableDeclarationExpression.getInitialiser();
        if (initialiser instanceof BoundArrayLiteralExpression) {
            BoundArrayLiteralExpression arrayLiteralExpression = (BoundArrayLiteralExpression) initialiser;
            return new BoundArrayVariableDeclarationExpression(rewrittenExpression.getVariable(), rewrittenExpression.getGuard(), arrayLiteralExpression, arrayLiteralExpression.getElements().size(), rewrittenExpression.isReadOnly());
        }

        throw new UnsupportedOperationException("Array declarations must have size available at compile time");
    }

    @Override
    protected BoundExpression rewriteForExpression(BoundForExpression boundForExpression) {
        BoundExpression rewrittenExpression = super.rewriteForExpression(boundForExpression);

        if (!(rewrittenExpression instanceof BoundForExpression)) {
            return rewrittenExpression;
        }
        BoundForExpression forExpression = (BoundForExpression) rewrittenExpression;

        VariableSymbol iterator = forExpression.getIterator();
        BoundRangeExpression rangeExpression = forExpression.getRangeExpression();

        BoundVariableDeclarationExpression initialisation = new BoundVariableDeclarationExpression(iterator, null, rangeExpression.getLowerBound(), false);

        BoundVariableExpression iteratorExpression = new BoundVariableExpression(iterator);

        BoundBinaryExpression condition;
        BoundExpression postStep;
        if (iteratorExpression.getType() == TypeSymbol.INT) {
            condition = new BoundBinaryExpression(iteratorExpression, BoundBinaryOperator.bind(OpType.LT, TypeSymbol.INT, TypeSymbol.INT), rangeExpression.getUpperBound());
            postStep = new BoundAssignmentExpression(iterator, null,
                    new BoundBinaryExpression(iteratorExpression, BoundBinaryOperator.bind(OpType.ADD, TypeSymbol.INT, TypeSymbol.INT), rangeExpression.getStep() == null ? new BoundLiteralExpression(1) : rangeExpression.getStep())
            );
        } else if (iteratorExpression.getType() == TypeSymbol.CHAR) {
            condition = new BoundBinaryExpression(iteratorExpression, BoundBinaryOperator.bind(OpType.LTEQ, TypeSymbol.INT, TypeSymbol.INT), rangeExpression.getUpperBound());
            postStep = new BoundAssignmentExpression(iterator, null,
                    new BoundBinaryExpression(iteratorExpression, BoundBinaryOperator.bind(OpType.ADD, TypeSymbol.INT, TypeSymbol.INT), rangeExpression.getStep() == null ? new BoundLiteralExpression(1) : rangeExpression.getStep())
            );
        } else if (iteratorExpression.getType() == TypeSymbol.REAL) {
            condition = new BoundBinaryExpression(iteratorExpression, BoundBinaryOperator.bind(OpType.LT, TypeSymbol.REAL, TypeSymbol.REAL), rangeExpression.getUpperBound());
            postStep = new BoundAssignmentExpression(iterator, null,
                    new BoundBinaryExpression(iteratorExpression, BoundBinaryOperator.bind(OpType.ADD, TypeSymbol.REAL, TypeSymbol.REAL), rangeExpression.getStep() == null ? new BoundLiteralExpression(1.0D) : rangeExpression.getStep())
            );
        } else {
            throw new UnsupportedOperationException("No such operation for types `" + iteratorExpression.getType() + "` and `" + rangeExpression.getUpperBound().getType() + "`");
        }

        return new BoundCStyleForExpression(initialisation, condition, postStep, rewriteExpression(forExpression.getBody()));
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
