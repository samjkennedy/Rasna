package com.skennedy.bixbite.lowering;

import com.skennedy.bixbite.parsing.model.OpType;
import com.skennedy.bixbite.typebinding.BoundForExpression;
import com.skennedy.bixbite.typebinding.BoundAssignmentExpression;
import com.skennedy.bixbite.typebinding.BoundBinaryExpression;
import com.skennedy.bixbite.typebinding.BoundBinaryOperator;
import com.skennedy.bixbite.typebinding.BoundBlockExpression;
import com.skennedy.bixbite.typebinding.BoundExpression;
import com.skennedy.bixbite.typebinding.BoundIfExpression;
import com.skennedy.bixbite.typebinding.BoundLiteralExpression;
import com.skennedy.bixbite.typebinding.BoundVariableDeclarationExpression;
import com.skennedy.bixbite.typebinding.BoundVariableExpression;
import com.skennedy.bixbite.typebinding.BoundWhileExpression;
import com.skennedy.bixbite.typebinding.TypeSymbol;

public class Lowerer extends BoundProgramRewriter {

    private static int labelCount = 0;

    @Override
    protected BoundExpression rewriteWhileExpression(BoundWhileExpression boundWhileExpression) {

        BoundExpression expression = super.rewriteWhileExpression(boundWhileExpression);

        if (expression instanceof BoundNoOpExpression) {
            return expression;
        }

        BoundLabel continueLabel = generateLabel();
        BoundLabel checkLabel = generateLabel();
        BoundLabel endLabel = generateLabel();

        BoundGotoExpression gotoCheck= new BoundGotoExpression(checkLabel);
        BoundLabelExpression continueLabelExpression = new BoundLabelExpression(continueLabel);
        BoundLabelExpression checkLabelExpression = new BoundLabelExpression(checkLabel);
        BoundLabelExpression endLabelExpression = new BoundLabelExpression(endLabel);
        BoundConditionalGotoExpression gotoTrue = new BoundConditionalGotoExpression(continueLabel, boundWhileExpression.getCondition(), false);

        return rewriteBlockExpression(new BoundBlockExpression(
                gotoCheck,
                continueLabelExpression,
                boundWhileExpression.getBody(),
                checkLabelExpression,
                gotoTrue,
                endLabelExpression
        ));
    }

    @Override
    protected BoundExpression rewriteForExpression(BoundForExpression boundForExpression) {

        BoundExpression expression = super.rewriteForExpression(boundForExpression);

        if (expression instanceof BoundNoOpExpression) {
            return expression;
        }

        if (flatten(boundForExpression.getBody()).getExpressions().isEmpty()) {
            return new BoundNoOpExpression();
        }

        BoundVariableDeclarationExpression variableDeclarationExpression = new BoundVariableDeclarationExpression(boundForExpression.getIterator(), null, boundForExpression.getInitialiser(), false);

        BoundVariableExpression variableExpression = new BoundVariableExpression(boundForExpression.getIterator());

        BoundExpression step;
        if (boundForExpression.getStep() == null) {
            step = new BoundAssignmentExpression(boundForExpression.getIterator(),
                    null,
                    new BoundBinaryExpression(
                    variableExpression,
                    BoundBinaryOperator.bind(OpType.ADD, TypeSymbol.INT, TypeSymbol.INT),
                    new BoundLiteralExpression(1)
            ));
        } else {
            step = new BoundAssignmentExpression(boundForExpression.getIterator(),
                    null,
                    new BoundBinaryExpression(
                            variableExpression,
                            BoundBinaryOperator.bind(OpType.ADD, TypeSymbol.INT, TypeSymbol.INT),
                            boundForExpression.getStep()
                    )
            );
        }

        BoundBlockExpression whileBody;
        if (boundForExpression.getRange() == null) {
            whileBody = new BoundBlockExpression(boundForExpression.getBody(), step);
        } else {
            BoundExpression rangeCheck = new BoundIfExpression(boundForExpression.getRange(), boundForExpression.getBody(), null);
            whileBody = new BoundBlockExpression(rangeCheck, step);
        }

        BoundBinaryExpression condition = new BoundBinaryExpression(variableExpression, BoundBinaryOperator.bind(OpType.LT, TypeSymbol.INT, TypeSymbol.INT), boundForExpression.getTerminator());

        BoundWhileExpression whileExpression = new BoundWhileExpression(condition, whileBody);
        BoundBlockExpression boundBlockExpression = new BoundBlockExpression(variableDeclarationExpression, whileExpression);

        return rewriteBlockExpression(boundBlockExpression);
    }

    @Override
    protected BoundExpression rewriteIfExpression(BoundIfExpression boundIfExpression) {
        BoundExpression expression = super.rewriteIfExpression(boundIfExpression);

        if (expression instanceof BoundNoOpExpression) {
            return expression;
        }

        BoundLabel endLabel = generateLabel();
        if (boundIfExpression.getElseBody() == null) {

            BoundConditionalGotoExpression gotoFalse = new BoundConditionalGotoExpression(endLabel, boundIfExpression.getCondition(), true);
            BoundLabelExpression endLabelStatement = new BoundLabelExpression(endLabel);
            BoundBlockExpression result = new BoundBlockExpression(gotoFalse, boundIfExpression.getBody(), endLabelStatement);
            return rewriteBlockExpression(result);
        } else {

            BoundLabel elseLabel = generateLabel();

            BoundConditionalGotoExpression gotoFalse = new BoundConditionalGotoExpression(elseLabel, boundIfExpression.getCondition(), true);
            BoundGotoExpression gotoEndStatement = new BoundGotoExpression(endLabel);
            BoundLabelExpression endLabelStatement = new BoundLabelExpression(endLabel);
            BoundLabelExpression elseLabelStatement = new BoundLabelExpression(elseLabel);

            BoundBlockExpression result = new BoundBlockExpression(
                    gotoFalse,
                    boundIfExpression.getBody(),
                    gotoEndStatement,
                    elseLabelStatement,
                    boundIfExpression.getElseBody(),
                    endLabelStatement
            );
            return flatten(rewriteBlockExpression(result));
        }
    }

    private static BoundLabel generateLabel() {
        String name = "label_" + ++labelCount;
        return new BoundLabel(name);
    }

}
