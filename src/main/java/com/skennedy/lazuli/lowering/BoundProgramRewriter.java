package com.skennedy.lazuli.lowering;

import com.skennedy.lazuli.exceptions.InfiniteLoopException;
import com.skennedy.lazuli.typebinding.BoundArrayAccessExpression;
import com.skennedy.lazuli.typebinding.BoundArrayAssignmentExpression;
import com.skennedy.lazuli.typebinding.BoundArrayLiteralExpression;
import com.skennedy.lazuli.typebinding.BoundAssignmentExpression;
import com.skennedy.lazuli.typebinding.BoundBinaryExpression;
import com.skennedy.lazuli.typebinding.BoundBinaryOperator;
import com.skennedy.lazuli.typebinding.BoundBlockExpression;
import com.skennedy.lazuli.typebinding.BoundConstDeclarationExpression;
import com.skennedy.lazuli.typebinding.BoundExpression;
import com.skennedy.lazuli.typebinding.BoundExpressionType;
import com.skennedy.lazuli.typebinding.BoundForExpression;
import com.skennedy.lazuli.typebinding.BoundForInExpression;
import com.skennedy.lazuli.typebinding.BoundFunctionCallExpression;
import com.skennedy.lazuli.typebinding.BoundFunctionDeclarationExpression;
import com.skennedy.lazuli.typebinding.BoundIfExpression;
import com.skennedy.lazuli.typebinding.BoundIncrementExpression;
import com.skennedy.lazuli.typebinding.BoundLiteralExpression;
import com.skennedy.lazuli.typebinding.BoundMatchCaseExpression;
import com.skennedy.lazuli.typebinding.BoundMatchExpression;
import com.skennedy.lazuli.typebinding.BoundPrintExpression;
import com.skennedy.lazuli.typebinding.BoundProgram;
import com.skennedy.lazuli.typebinding.BoundReturnExpression;
import com.skennedy.lazuli.typebinding.BoundTypeofExpression;
import com.skennedy.lazuli.typebinding.BoundVariableDeclarationExpression;
import com.skennedy.lazuli.typebinding.BoundVariableExpression;
import com.skennedy.lazuli.typebinding.BoundWhileExpression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;

public abstract class BoundProgramRewriter {

    //So that all branches of a block can point to the same end label
    protected static BoundLabel blockEndLabel;

    public BoundProgram rewrite(BoundProgram program) {

        List<BoundExpression> rewrittenExpressions = new ArrayList<>();
        for (BoundExpression expression : program.getExpressions()) {
            BoundExpression rewrittenExpression = rewriteExpression(expression);
            if (rewrittenExpression instanceof BoundBlockExpression) {
                rewrittenExpressions.addAll(((BoundBlockExpression) rewrittenExpression).getExpressions());
            } else {
                rewrittenExpressions.add(rewrittenExpression);
            }
            blockEndLabel = null;
        }

        BoundBlockExpression nestedProgram = new BoundBlockExpression(rewrittenExpressions);

        return new BoundProgram(flatten(nestedProgram).getExpressions(), program.getErrors());
    }


    BoundBlockExpression flatten(BoundExpression root) {
        Stack<BoundExpression> stack = new Stack<>();
        List<BoundExpression> instructions = new ArrayList<>();

        stack.push(root);
        while (!stack.isEmpty()) {
            BoundExpression current = stack.pop();
            if (current.getBoundExpressionType() == BoundExpressionType.BLOCK) {
                List<BoundExpression> expressions = ((BoundBlockExpression) current).getExpressions();
                Collections.reverse(expressions);
                for (BoundExpression expression : expressions) {
                    stack.push(expression);
                }
            } else {
                instructions.add(current);
            }
        }

        return new BoundBlockExpression(instructions);
    }

    private BoundExpression rewriteExpression(BoundExpression expression) {
        switch (expression.getBoundExpressionType()) {

            case ARRAY_LITERAL_EXPRESSION:
                return rewriteArrayLiteralExpression((BoundArrayLiteralExpression) expression);
            case ARRAY_ACCESS_EXPRESSION:
                return rewriteArrayAccessExpression((BoundArrayAccessExpression) expression);
            case ARRAY_ASSIGNMENT_EXPRESSION:
                return rewriteArrayAssignmentExpression((BoundArrayAssignmentExpression) expression);
            case ARRAY_LENGTH_EXPRESSION:
            case LITERAL:
            case VARIABLE_EXPRESSION:
            case GOTO:
            case LABEL:
            case NOOP:
            case INCREMENT:
                return expression;
            case ASSIGNMENT_EXPRESSION:
                return rewriteAssignmentExpression((BoundAssignmentExpression) expression);
            case BINARY_EXPRESSION:
                return rewriteBinaryExpression((BoundBinaryExpression) expression);
            case BINARY_OPERATOR:
                throw new IllegalStateException("Unhandled bound expression type: " + expression.getBoundExpressionType());
            case BLOCK:
                return rewriteBlockExpression((BoundBlockExpression) expression);
            case IF:
                return rewriteIfExpression((BoundIfExpression) expression);
            case FOR:
                return rewriteForExpression((BoundForExpression) expression);
            case FOR_IN:
                return rewriteForInExpression((BoundForInExpression) expression);
            case PRINT_INTRINSIC:
                return rewritePrintIntrinsic((BoundPrintExpression) expression);
            case TYPEOF_INTRINSIC:
                return rewriteTypeofIntrinsic((BoundTypeofExpression) expression);
            case VARIABLE_DECLARATION:
                return rewriteVariableDeclaration((BoundVariableDeclarationExpression) expression);
            case WHILE:
                return rewriteWhileExpression((BoundWhileExpression) expression);
            case CONDITIONAL_GOTO:
                return rewriteConditionalGoto((BoundConditionalGotoExpression) expression);
            case FUNCTION_CALL:
                return rewriteFunctionCall((BoundFunctionCallExpression) expression);
            case FUNCTION_DECLARATION:
                return rewriteFunctionDeclaration((BoundFunctionDeclarationExpression) expression); //TODO: rewrite parameters
            case RETURN:
                return rewriteReturnCall((BoundReturnExpression) expression);
            case MATCH_EXPRESSION:
                return rewriteMatchExpression((BoundMatchExpression) expression);
            default:
                throw new IllegalStateException("Unexpected value: " + expression.getBoundExpressionType());
        }
    }

    protected BoundExpression rewriteMatchExpression(BoundMatchExpression matchExpression) {

        if (matchExpression.getMatchCaseExpressions().isEmpty()) {
            return new BoundNoOpExpression();
        }

        BoundExpression rewrittenOperand = rewriteExpression(matchExpression.getOperand());

        List<BoundMatchCaseExpression> rewrittenCaseExpressions = new ArrayList<>();
        for (BoundMatchCaseExpression caseExpression : matchExpression.getMatchCaseExpressions()) {
            rewrittenCaseExpressions.add(rewriteMatchCaseExpression(caseExpression));
        }

        if (rewrittenCaseExpressions != matchExpression.getMatchCaseExpressions()
                || rewrittenOperand != matchExpression.getOperand()) {
            return new BoundMatchExpression(matchExpression.getType(), rewrittenOperand, rewrittenCaseExpressions);
        }
        return matchExpression;
    }

    protected BoundMatchCaseExpression rewriteMatchCaseExpression(BoundMatchCaseExpression matchCaseExpression) {
        BoundExpression rewrittenCaseExpression = null;
        if (matchCaseExpression.getCaseExpression() != null) {
            rewrittenCaseExpression = rewriteExpression(matchCaseExpression.getCaseExpression());
        }
        BoundExpression rewrittenThenExpression = rewriteExpression(matchCaseExpression.getThenExpression());

        if (rewrittenCaseExpression != matchCaseExpression.getCaseExpression()
                || rewrittenThenExpression != matchCaseExpression.getThenExpression()) {
            return new BoundMatchCaseExpression(rewrittenCaseExpression, rewrittenThenExpression);
        }

        return matchCaseExpression;
    }

    private BoundExpression rewriteReturnCall(BoundReturnExpression returnExpression) {
        BoundExpression returnValue = rewriteExpression(returnExpression.getReturnValue());

        if (returnExpression.getReturnValue() instanceof BoundIfExpression) {

            List<BoundExpression> expressions = ((BoundBlockExpression) returnValue).getExpressions();

            for (int i = 0; i < expressions.size(); i++) {
                BoundExpression expression = expressions.get(i);
                if (expression instanceof BoundLiteralExpression) {
                    expressions.set(i, new BoundReturnExpression(expression));
                }
            }
            return new BoundBlockExpression(expressions);
        }

        if (returnValue != returnExpression.getReturnValue()) {
            return new BoundReturnExpression(returnValue);
        }

        return returnExpression;
    }

    protected BoundExpression rewriteFunctionDeclaration(BoundFunctionDeclarationExpression functionDeclarationExpression) {

        List<BoundExpression> rewrittenInstructions = new ArrayList<>();
        for (BoundExpression expression : functionDeclarationExpression.getBody().getExpressions()) {
            rewrittenInstructions.add(rewriteExpression(expression));
        }

        BoundBlockExpression rewrittenBody = flatten(rewriteBlockExpression(new BoundBlockExpression(rewrittenInstructions)));

        if (rewrittenBody != functionDeclarationExpression.getBody()) {
            return new BoundFunctionDeclarationExpression(functionDeclarationExpression.getFunctionSymbol(), functionDeclarationExpression.getArguments(), rewrittenBody);
        }
        return functionDeclarationExpression;
    }

    protected BoundExpression rewriteFunctionCall(BoundFunctionCallExpression functionCallExpression) {

        List<BoundExpression> rewrittenArgs = new ArrayList<>();
        for (BoundExpression arg : functionCallExpression.getBoundArguments()) {
            rewrittenArgs.add(rewriteExpression(arg));
        }

        if (rewrittenArgs != functionCallExpression.getBoundArguments()) {
            return new BoundFunctionCallExpression(functionCallExpression.getFunction(), rewrittenArgs);
        }
        return functionCallExpression;
    }

    private BoundExpression rewriteArrayLiteralExpression(BoundArrayLiteralExpression arrayLiteralExpression) {

        List<BoundExpression> rewrittenElements = new ArrayList<>();

        boolean rewritten = false;
        for (BoundExpression element : arrayLiteralExpression.getElements()) {

            BoundExpression rewrittenElement = rewriteExpression(element);
            rewrittenElements.add(rewrittenElement);
            if (rewrittenElement != element) {
                rewritten = true;
            }
        }

        if (rewritten) {
            return new BoundArrayLiteralExpression(rewrittenElements);
        }
        return arrayLiteralExpression;
    }

    private BoundExpression rewriteArrayAccessExpression(BoundArrayAccessExpression arrayAccessExpression) {

        BoundExpression index = rewriteExpression(arrayAccessExpression.getIndex());

        if (index == arrayAccessExpression.getIndex()) {
            return arrayAccessExpression;
        }
        return new BoundArrayAccessExpression(arrayAccessExpression.getArray(), index);
    }

    private BoundExpression rewriteArrayAssignmentExpression(BoundArrayAssignmentExpression arrayAssignmentExpression) {

        BoundExpression arrayAccessExpression = rewriteArrayAccessExpression(arrayAssignmentExpression.getArrayAccessExpression());
        BoundExpression assignment = rewriteExpression(arrayAssignmentExpression.getAssignment());

        if (arrayAccessExpression == arrayAssignmentExpression.getArrayAccessExpression()
                && assignment == arrayAssignmentExpression.getAssignment()) {
            return arrayAssignmentExpression;
        }
        return new BoundArrayAssignmentExpression(arrayAssignmentExpression.getArrayAccessExpression(), arrayAssignmentExpression.getAssignment());
    }

    private BoundExpression rewriteAssignmentExpression(BoundAssignmentExpression assignmentExpression) {

        BoundExpression expression = rewriteExpression(assignmentExpression.getExpression());

        if (expression instanceof BoundIncrementExpression) {
            return expression;
        }

        //TODO: This can let you sneak past a variable's guard I think
        if (expression instanceof BoundBinaryExpression) {
            BoundBinaryExpression binaryExpression = (BoundBinaryExpression) expression;

            if (binaryExpression.getLeft().getBoundExpressionType() == BoundExpressionType.VARIABLE_EXPRESSION
                    && binaryExpression.getRight().getBoundExpressionType() == BoundExpressionType.LITERAL) {

                BoundVariableExpression variableExpression = (BoundVariableExpression) binaryExpression.getLeft();
                BoundLiteralExpression literalExpression = (BoundLiteralExpression) binaryExpression.getRight();

                if ((int) literalExpression.getValue() <= Byte.MAX_VALUE && (int)literalExpression.getValue() >= Byte.MIN_VALUE) {

                    if (binaryExpression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.ADDITION) {
                        return new BoundIncrementExpression(variableExpression.getVariable(), new BoundLiteralExpression(literalExpression.getValue()));

                    } else if (binaryExpression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.SUBTRACTION) {
                        return new BoundIncrementExpression(variableExpression.getVariable(), new BoundLiteralExpression(-(int) literalExpression.getValue()));
                    }
                }
            }
            return assignmentExpression;
        }

        BoundExpression guard = null;
        if (assignmentExpression.getGuard() != null) {
            guard = rewriteExpression(assignmentExpression.getGuard());
        }

        if (expression == assignmentExpression.getExpression() && guard == assignmentExpression.getGuard()) {
            return assignmentExpression;
        }

        if (expression instanceof BoundBlockExpression) {

            return rewriteBlockInitialiser(
                    assignmentExpression.getExpression(),
                    (BoundBlockExpression) expression,
                    expr -> new BoundAssignmentExpression(assignmentExpression.getVariable(), assignmentExpression.getGuard(), expr));
        }
        return new BoundAssignmentExpression(assignmentExpression.getVariable(), assignmentExpression.getGuard(), assignmentExpression.getExpression());
    }

    protected BoundExpression rewriteBinaryExpression(BoundBinaryExpression boundBinaryExpression) {

        BoundExpression left = rewriteExpression(boundBinaryExpression.getLeft());
        BoundExpression right = rewriteExpression(boundBinaryExpression.getRight());

        //Both sides are constant, can do constant folding
        if (left instanceof BoundLiteralExpression && right instanceof BoundLiteralExpression) {

            return calculateConstant(((BoundLiteralExpression) left).getValue(), ((BoundLiteralExpression) right).getValue(), boundBinaryExpression.getOperator());
        }

        if (left == boundBinaryExpression.getLeft() && right == boundBinaryExpression.getRight()) {
            return boundBinaryExpression;
        }

        return new BoundBinaryExpression(left, boundBinaryExpression.getOperator(), right);
    }

    BoundExpression rewriteBlockExpression(BoundBlockExpression boundBlockExpression) {
        if (boundBlockExpression.getExpressions().isEmpty()) {
            return new BoundNoOpExpression();
        }
        List<BoundExpression> rewrittenExpressions = new ArrayList<>();

        for (BoundExpression boundExpression : boundBlockExpression.getExpressions()) {
            rewrittenExpressions.add(rewriteExpression(boundExpression));
        }
        return new BoundBlockExpression(rewrittenExpressions);
    }

    protected BoundExpression rewriteIfExpression(BoundIfExpression boundIfExpression) {

        BoundExpression condition = rewriteExpression(boundIfExpression.getCondition());
        BoundExpression body = rewriteExpression(boundIfExpression.getBody());


        BoundExpression elseBody = null;
        if (boundIfExpression.getElseBody() != null) {
            elseBody = rewriteExpression(boundIfExpression.getElseBody());
        }

        //TODO: in the case of a const variable expression (const variable lol) currently cannot determine the const value + errors here,
        //TODO: FIX
        if (condition.getBoundExpressionType() == BoundExpressionType.LITERAL || condition.getBoundExpressionType() == BoundExpressionType.VARIABLE_EXPRESSION && ((BoundVariableExpression) condition).getVariable().isReadOnly()) {
            boolean constValue = (boolean) calculateConstant(condition);
            if (constValue) {
                return body;
            } else if (elseBody != null) {
                return elseBody;
            } else {
                return new BoundNoOpExpression();
            }
        }

        if (condition == boundIfExpression.getCondition() && body == boundIfExpression.getBody() && elseBody == boundIfExpression.getElseBody()) {
            return boundIfExpression;
        }

        return new BoundIfExpression(condition, body, elseBody);
    }

    protected BoundExpression rewriteForExpression(BoundForExpression forExpression) {

        BoundExpression initialiser = rewriteExpression(forExpression.getInitialiser());
        BoundExpression terminator = rewriteExpression(forExpression.getTerminator());
        BoundExpression step = null;
        if (forExpression.getStep() != null) {
            step = rewriteExpression(forExpression.getStep());
        }
        BoundExpression guard = null;
        if (forExpression.getGuard() != null) {
            guard = rewriteExpression(forExpression.getGuard());
        }
        BoundExpression body = rewriteExpression(forExpression.getBody());

        if (body instanceof BoundNoOpExpression) {
            return new BoundNoOpExpression();
        }

        if (initialiser == forExpression.getInitialiser()
                && terminator == forExpression.getTerminator()
                && step == forExpression.getStep()
                && guard == forExpression.getGuard()
                && body == forExpression.getBody()) {
            return forExpression;
        }

        return new BoundForExpression(forExpression.getIterator(), initialiser, terminator, step, guard, body);
    }

    protected BoundExpression rewriteForInExpression(BoundForInExpression forInExpression) {

        BoundExpression iterable = rewriteExpression(forInExpression.getIterable());
        BoundExpression guard = null;
        if (forInExpression.getGuard() != null) {
            guard = rewriteExpression(forInExpression.getGuard());
        }
        BoundExpression body = rewriteExpression(forInExpression.getBody());

        if (body instanceof BoundNoOpExpression) {
            return new BoundNoOpExpression();
        }

        if (iterable == forInExpression.getIterable()
                && guard == forInExpression.getGuard()
                && body == forInExpression.getBody()) {
            return forInExpression;
        }

        return new BoundForInExpression(forInExpression.getVariable(), iterable, guard, body);
    }

    private BoundExpression rewriteVariableDeclaration(BoundVariableDeclarationExpression boundVariableDeclarationExpression) {

        BoundExpression initialiser = null;
        if (boundVariableDeclarationExpression.getInitialiser() != null) {
            initialiser = rewriteExpression(boundVariableDeclarationExpression.getInitialiser());
        }
        BoundExpression guard = boundVariableDeclarationExpression.getGuard() == null
                ? null
                : rewriteExpression(boundVariableDeclarationExpression.getGuard());

        if (boundVariableDeclarationExpression.isReadOnly()) {
            return new BoundConstDeclarationExpression(
                    boundVariableDeclarationExpression.getVariable(),
                    new BoundLiteralExpression(calculateConstant(boundVariableDeclarationExpression.getInitialiser()))
            );
        }

        if (initialiser == boundVariableDeclarationExpression.getInitialiser() && guard == boundVariableDeclarationExpression.getGuard()) {
            return boundVariableDeclarationExpression;
        }

        //TODO: The following three are more of the conditional's problem, not the assignments, since this should be the default behaviour

        if (initialiser instanceof BoundBlockExpression) {
            BoundExpression blockInitialiser = rewriteBlockInitialiser(
                    boundVariableDeclarationExpression.getInitialiser(),
                    (BoundBlockExpression) initialiser,
                    expr -> new BoundAssignmentExpression(boundVariableDeclarationExpression.getVariable(), boundVariableDeclarationExpression.getGuard(), expr)
            );

            BoundVariableDeclarationExpression tempInit = new BoundVariableDeclarationExpression(boundVariableDeclarationExpression.getVariable(), boundVariableDeclarationExpression.getGuard(), new BoundLiteralExpression(0), false);

            return new BoundBlockExpression(
                    tempInit,
                    blockInitialiser
            );

        }
        return boundVariableDeclarationExpression;
    }

    protected BoundExpression rewriteWhileExpression(BoundWhileExpression boundWhileExpression) {

        BoundExpression condition = rewriteExpression(boundWhileExpression.getCondition());
        BoundExpression body = rewriteExpression(boundWhileExpression.getBody());

        if (body instanceof BoundNoOpExpression) {
            return new BoundNoOpExpression();
        }

        if (condition.getBoundExpressionType() == BoundExpressionType.LITERAL || condition.getBoundExpressionType() == BoundExpressionType.VARIABLE_EXPRESSION && ((BoundVariableExpression) condition).getVariable().isReadOnly()) {
            boolean constValue = (boolean) calculateConstant(condition);
            if (!constValue) {
                return new BoundNoOpExpression();
            } else {
                throw new InfiniteLoopException();
            }
        }

        if (condition == boundWhileExpression.getCondition() && body == boundWhileExpression.getBody()) {
            return boundWhileExpression;
        }
        return new BoundWhileExpression(condition, body);
    }

    private Object calculateConstant(BoundExpression expression) {
        switch (expression.getBoundExpressionType()) {

            case LITERAL:
                BoundLiteralExpression boundLiteralExpression = (BoundLiteralExpression) expression;
                return boundLiteralExpression.getValue();
            case VARIABLE_EXPRESSION:
                return expression;
            case BINARY_EXPRESSION:
                BoundBinaryExpression boundBinaryExpression = (BoundBinaryExpression) expression;
                return calculateConstant(calculateConstant(boundBinaryExpression.getLeft()), calculateConstant(boundBinaryExpression.getRight()), boundBinaryExpression.getOperator());
            default:
                throw new IllegalStateException("Unexpected expression type for constant folding: " + expression.getBoundExpressionType());
        }
    }

    private BoundExpression rewriteConditionalGoto(BoundConditionalGotoExpression conditionalGotoExpression) {
        BoundExpression condition = rewriteExpression(conditionalGotoExpression.getCondition());
        if (condition == conditionalGotoExpression.getCondition()) {
            return conditionalGotoExpression;
        }
        return new BoundConditionalGotoExpression(conditionalGotoExpression.getLabel(), condition, conditionalGotoExpression.jumpIfFalse());
    }

    private BoundExpression calculateConstant(Object left, Object right, BoundBinaryOperator operator) {
        switch (operator.getBoundOpType()) {

            case ADDITION:
                return new BoundLiteralExpression((int) left + (int) right);
            case SUBTRACTION:
                return new BoundLiteralExpression((int) left - (int) right);
            case MULTIPLICATION:
                return new BoundLiteralExpression((int) left * (int) right);
            case DIVISION:
                return new BoundLiteralExpression((int) left / (int) right);
            case REMAINDER:
                return new BoundLiteralExpression((int) left % (int) right);
            case GREATER_THAN:
                return new BoundLiteralExpression((int) left > (int) right);
            case LESS_THAN:
                return new BoundLiteralExpression((int) left < (int) right);
            case GREATER_THAN_OR_EQUAL:
                return new BoundLiteralExpression((int) left >= (int) right);
            case LESS_THAN_OR_EQUAL:
                return new BoundLiteralExpression((int) left <= (int) right);
            case EQUALS:
                return new BoundLiteralExpression((int) left == (int) right);
            case NOT_EQUALS:
                return new BoundLiteralExpression((int) left != (int) right);
            case BOOLEAN_OR:
                return new BoundLiteralExpression((boolean) left || (boolean) right);
            case BOOLEAN_AND:
                return new BoundLiteralExpression((boolean) left && (boolean) right);
            default:
                throw new IllegalStateException("Unhandled bound binary operator for constant folding: " + operator.getBoundOpType());
        }
    }


    private BoundExpression rewriteTypeofIntrinsic(BoundTypeofExpression typeofExpression) {

        BoundExpression expression = rewriteExpression(typeofExpression.getExpression());

        if (expression == typeofExpression.getExpression()) {
            return typeofExpression;
        }
        return new BoundTypeofExpression(typeofExpression);
    }

    private BoundExpression rewritePrintIntrinsic(BoundPrintExpression printExpression) {

        BoundExpression expression = rewriteExpression(printExpression.getExpression());

        if (expression == printExpression.getExpression()) {
            return printExpression;
        }
        return new BoundPrintExpression(expression);
    }

    private static <T extends BoundExpression> BoundExpression rewriteBlockInitialiser(BoundExpression originalInitialiser, BoundBlockExpression initialiser, Function<BoundExpression, T> remapper) {

        if (originalInitialiser instanceof BoundIfExpression || originalInitialiser instanceof BoundMatchExpression) {

            List<BoundExpression> expressions = initialiser.getExpressions();

            for (int i = 0; i < expressions.size(); i++) {
                BoundExpression expr = expressions.get(i);
                if (expr instanceof BoundLiteralExpression) {
                    expressions.set(i, remapper.apply(expr));
                } else if (expr instanceof BoundBinaryExpression) {
                    expressions.set(i, remapper.apply(expr));
                }
            }
            return new BoundBlockExpression(expressions);

        }
        if (originalInitialiser instanceof BoundForExpression) {

            //Int[] arr = for (Int x = 0 to 10) {
            //  x * x
            //}

            throw new UnsupportedOperationException("For expression assignment is not yet supported");
        }
        if (originalInitialiser instanceof BoundForInExpression) {

            //Int[] arr = for (Int x in iterable) {
            //  x * x
            //}

            throw new UnsupportedOperationException("For-in expression assignment is not yet supported");
        }
        if (originalInitialiser instanceof BoundWhileExpression) {

            //Int a = 0
            //Int[] arr = while (condition) {
            //  a = a + 1
            //  return a
            //}

            throw new UnsupportedOperationException("While expression assignment is not yet supported");
        }
        throw new UnsupportedOperationException("Assigning " + originalInitialiser.getBoundExpressionType() + " is not yet supported");
    }

}
