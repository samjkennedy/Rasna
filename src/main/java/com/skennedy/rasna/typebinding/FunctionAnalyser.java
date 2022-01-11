package com.skennedy.rasna.typebinding;

import com.skennedy.rasna.diagnostics.BindingError;
import com.skennedy.rasna.parsing.BlockExpression;
import com.skennedy.rasna.parsing.Expression;
import com.skennedy.rasna.parsing.FunctionDeclarationExpression;

import java.util.ArrayList;
import java.util.List;

public class FunctionAnalyser {

    public static List<BindingError> analyzeBody(FunctionSymbol function, List<BoundExpression> boundExpressions, List<Expression> expressions, FunctionDeclarationExpression functionDeclarationExpression) {

        List<BindingError> errors = new ArrayList<>(typeCheckBody(function, boundExpressions, expressions));

        if (function.getType() != TypeSymbol.VOID) {
            boolean allPathsReturnValue = checkReturnPaths(boundExpressions);
            if (!allPathsReturnValue) {
                errors.add(BindingError.raiseMissingReturnExpression(function.getName(), functionDeclarationExpression.getBody().getCloseCurly().getSpan()));
            }
        }

        return errors;
    }

    private static boolean checkReturnPaths(List<BoundExpression> boundExpressions) {

        boolean allPathsReturnValue = false;
        for (int i = 0; i < boundExpressions.size(); i++) {

            BoundExpression boundExpression = boundExpressions.get(i);
            allPathsReturnValue = checkReturnPath(boundExpression);
        }

        return allPathsReturnValue;
    }

    private static boolean checkReturnPath(BoundExpression boundExpression) {

        boolean pathReturnsValue = false;
        switch (boundExpression.getBoundExpressionType()) {
            case IF:
                pathReturnsValue = checkReturnPaths((BoundIfExpression) boundExpression);
                break;
            case BLOCK:
                pathReturnsValue = checkReturnPaths(((BoundBlockExpression) boundExpression).getExpressions());
                break;
            case RETURN:
                pathReturnsValue = true;
                break;
            case WHILE:
                pathReturnsValue = checkReturnPaths((BoundWhileExpression) boundExpression);
                break;
            case MATCH_EXPRESSION:
                pathReturnsValue = checkReturnPaths((BoundMatchExpression) boundExpression);
                break;
        }

        return pathReturnsValue;
    }

    private static boolean checkReturnPaths(BoundMatchExpression matchExpression) {

        if (matchExpression.getMatchCaseExpressions().isEmpty()) {
            return false;
        }

        boolean allCasesReturnValue = true;
        for (BoundMatchCaseExpression matchCaseExpression : matchExpression.getMatchCaseExpressions()) {
            allCasesReturnValue = allCasesReturnValue && checkReturnPath(matchCaseExpression.getThenExpression());
        }
        return allCasesReturnValue;
    }

    private static boolean checkReturnPaths(BoundWhileExpression whileExpression) {

        //TODO: Once you can break out of loops this isn't the case
        if (whileExpression.getCondition().isConstExpression() && (boolean) whileExpression.getCondition().getConstValue()) {
            return checkReturnPath(whileExpression.getBody());
        }
        return false;
    }


    private static boolean checkReturnPaths(BoundIfExpression ifExpression) {

        if (ifExpression.getCondition().isConstExpression() && (boolean) ifExpression.getCondition().getConstValue()) {
            return checkReturnPath(ifExpression.getBody());
        }
        if (ifExpression.getElseBody() != null) {
            return checkReturnPath(ifExpression.getBody()) && checkReturnPath(ifExpression.getElseBody());
        }
        return false;
    }

    private static List<BindingError> typeCheckBody(FunctionSymbol function, List<BoundExpression> boundExpressions, List<Expression> expressions) {

        List<BindingError> errors = new ArrayList<>();

        TypeSymbol returnType = function.getType();
        for (int i = 0; i < boundExpressions.size(); i++) {

            BoundExpression boundExpression = boundExpressions.get(i);
            if (boundExpression.getBoundExpressionType() == BoundExpressionType.RETURN) {

                if (!boundExpression.getType().isAssignableFrom(returnType)) {
                    errors.add(BindingError.raiseTypeMismatch(returnType, boundExpression.getType(), expressions.get(i).getSpan()));
                }
            }
            if (boundExpression.getBoundExpressionType() == BoundExpressionType.BLOCK) {
                errors.addAll(analyzeBlock((BoundBlockExpression) boundExpression, (BlockExpression) expressions.get(i), returnType));
            }
        }
        return errors;
    }

    private static List<BindingError> analyzeBlock(BoundBlockExpression boundBlock, BlockExpression block, TypeSymbol returnType) {

        List<BindingError> errors = new ArrayList<>();

        for (int i = 0; i < boundBlock.getExpressions().size(); i++) {
            BoundExpression boundExpression = boundBlock.getExpressions().get(i);
            if (boundExpression.getBoundExpressionType() == BoundExpressionType.RETURN) {

                BoundReturnExpression returnExpression = (BoundReturnExpression) boundExpression;
                if (!returnExpression.getType().isAssignableFrom(returnType)) {
                    errors.add(BindingError.raiseTypeMismatch(returnType, returnExpression.getType(), block.getExpressions().get(i).getSpan()));
                }
            }
            if (boundExpression.getBoundExpressionType() == BoundExpressionType.BLOCK) {
                analyzeBlock((BoundBlockExpression) boundExpression, (BlockExpression) block.getExpressions().get(i), returnType);
            }
        }

        return errors;
    }
}
