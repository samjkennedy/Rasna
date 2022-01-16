package com.skennedy.rasna.lowering;

import com.skennedy.rasna.parsing.model.OpType;
import com.skennedy.rasna.typebinding.*;

import java.util.UUID;

public abstract class Lowerer extends BoundProgramRewriter {

    @Override
    protected BoundExpression rewriteForInExpression(BoundForInExpression forInExpression) {
        BoundExpression expression = super.rewriteForInExpression(forInExpression);

        if (expression instanceof BoundNoOpExpression) {
            return expression;
        }
        if (!(expression instanceof BoundForInExpression)) {
            return expression;
        }
        BoundForInExpression rewrittenForInExpression = (BoundForInExpression) expression;

        if (rewrittenForInExpression.getBody() instanceof BoundNoOpExpression) {
            return new BoundNoOpExpression();
        }
        VariableSymbol arrayLength = new VariableSymbol("array-length-" + generateInternalVariableName(), TypeSymbol.INT, null, false, null);
        BoundVariableExpression arrayLengthExpression = new BoundVariableExpression(arrayLength);
        VariableSymbol iterationCounter = new VariableSymbol("iteration-counter-" + generateInternalVariableName(), TypeSymbol.INT, null, false, null);
        BoundVariableExpression iterationCounterExpression = new BoundVariableExpression(iterationCounter);

        BoundBlockExpression preLoop = new BoundBlockExpression(
                new BoundVariableDeclarationExpression( //Store array length
                        arrayLength,
                        null,
                        new BoundArrayLengthExpression(forInExpression.getIterable()),
                        false
                ),
                new BoundVariableDeclarationExpression(//Initialise internal counter
                        iterationCounter,
                        null,
                        new BoundLiteralExpression(0),
                        false
                ),
                new BoundVariableDeclarationExpression( //Initialise iterator
                        ((BoundForInExpression) expression).getVariable(),
                        ((BoundForInExpression) expression).getGuard(),
                        new BoundPositionalAccessExpression(((BoundForInExpression) expression).getIterable(), new BoundLiteralExpression(0)),
                        false
                )
        );

        BoundBlockExpression loopAssign = new BoundBlockExpression(
                new BoundAssignmentExpression(
                        ((BoundForInExpression) expression).getVariable(),
                        ((BoundForInExpression) expression).getGuard(),
                        new BoundPositionalAccessExpression(((BoundForInExpression) expression).getIterable(), iterationCounterExpression)
                )
        );

        BoundExpression step = new BoundIncrementExpression(iterationCounter, new BoundLiteralExpression(1));

        BoundBlockExpression whileBody;
        if (rewrittenForInExpression.getGuard() == null) {
            whileBody = new BoundBlockExpression(loopAssign, rewrittenForInExpression.getBody(), step);
        } else {
            BoundBlockExpression body = new BoundBlockExpression(rewrittenForInExpression.getBody());
            BoundExpression guardClause = new BoundIfExpression(rewrittenForInExpression.getGuard(), body, null);
            whileBody = new BoundBlockExpression(loopAssign, guardClause, step);
        }

        BoundBinaryExpression condition = new BoundBinaryExpression(iterationCounterExpression, BoundBinaryOperator.bind(OpType.LT, TypeSymbol.INT, TypeSymbol.INT), arrayLengthExpression);
        BoundWhileExpression whileExpression = new BoundWhileExpression(condition, whileBody);

        BoundBlockExpression boundBlockExpression = new BoundBlockExpression(preLoop, whileExpression);

        return flatten(rewriteBlockExpression(boundBlockExpression));
    }

    protected static String generateInternalVariableName() {
        return UUID.randomUUID().toString();
    }
}
