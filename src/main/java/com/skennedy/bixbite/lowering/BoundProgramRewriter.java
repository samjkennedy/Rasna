package com.skennedy.bixbite.lowering;

import com.skennedy.bixbite.typebinding.BoundProgram;
import com.skennedy.bixbite.exceptions.InfiniteLoopException;
import com.skennedy.bixbite.typebinding.BoundAssignmentExpression;
import com.skennedy.bixbite.typebinding.BoundBinaryExpression;
import com.skennedy.bixbite.typebinding.BoundBinaryOperator;
import com.skennedy.bixbite.typebinding.BoundBlockExpression;
import com.skennedy.bixbite.typebinding.BoundConstDeclarationExpression;
import com.skennedy.bixbite.typebinding.BoundExpression;
import com.skennedy.bixbite.typebinding.BoundExpressionType;
import com.skennedy.bixbite.typebinding.BoundForExpression;
import com.skennedy.bixbite.typebinding.BoundIfExpression;
import com.skennedy.bixbite.typebinding.BoundLiteralExpression;
import com.skennedy.bixbite.typebinding.BoundPrintExpression;
import com.skennedy.bixbite.typebinding.BoundTypeofExpression;
import com.skennedy.bixbite.typebinding.BoundVariableDeclarationExpression;
import com.skennedy.bixbite.typebinding.BoundVariableExpression;
import com.skennedy.bixbite.typebinding.BoundWhileExpression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public abstract class BoundProgramRewriter {

    public BoundProgram rewrite(BoundProgram program) {

        List<BoundExpression> rewrittenExpressions = new ArrayList<>();
        for (BoundExpression expression : program.getExpressions()) {
            BoundExpression rewrittenExpression = rewriteExpression(expression);
            if (rewrittenExpression instanceof BoundBlockExpression) {
                rewrittenExpressions.addAll(((BoundBlockExpression)rewrittenExpression).getExpressions());
            } else {
                rewrittenExpressions.add(rewrittenExpression);
            }
        }

        BoundBlockExpression nestedProgram = new BoundBlockExpression(rewrittenExpressions);

        return new BoundProgram(flatten(nestedProgram).getExpressions(), program.getErrors());
    }


    protected BoundBlockExpression flatten(BoundExpression root) {
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
            case LITERAL:
            case VARIABLE_EXPRESSION:
            case GOTO:
                return expression;
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
            case NOOP:
            case LABEL:
                return expression;
            default:
                throw new IllegalStateException("Unexpected value: " + expression.getBoundExpressionType());
        }
    }

    private BoundExpression rewriteAssignmentExpression(BoundAssignmentExpression assignmentExpression) {

        BoundExpression expression = rewriteExpression(assignmentExpression.getExpression());
        BoundExpression range = null;
        if (assignmentExpression.getRange() != null) {
            range = rewriteExpression(assignmentExpression.getRange());
        }

        if (expression instanceof BoundBlockExpression) {
            List<BoundExpression> expressions = ((BoundBlockExpression)expression).getExpressions();

            for (int i = 0; i < expressions.size(); i++) {
                BoundExpression expr = expressions.get(i);
                if (expr instanceof BoundLiteralExpression) {
                    expressions.set(i, new BoundAssignmentExpression(assignmentExpression.getVariable(), assignmentExpression.getRange(), expr));
                } else if (expr instanceof BoundBinaryExpression) {
                    expressions.set(i, new BoundAssignmentExpression(assignmentExpression.getVariable(), assignmentExpression.getRange(), expr));
                }
            }
            return new BoundBlockExpression(expressions);
        }

        if (expression == assignmentExpression.getExpression() && range == assignmentExpression.getRange()) {
            return assignmentExpression;
        }
        return new BoundAssignmentExpression(assignmentExpression.getVariable(), assignmentExpression.getRange(), assignmentExpression.getExpression());
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

    protected BoundExpression rewriteBlockExpression(BoundBlockExpression boundBlockExpression) {
        if (boundBlockExpression.getExpressions().isEmpty()) {
            return new BoundNoOpExpression();
        }
        //TODO: Return a special NOOP expression if the block is empty
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
        BoundExpression range = null;
        if (forExpression.getRange() != null) {
            range = rewriteExpression(forExpression.getRange());
        }
        BoundExpression body = rewriteExpression(forExpression.getBody());

        if (body instanceof BoundNoOpExpression) {
            return new BoundNoOpExpression();
        }

        if (initialiser == forExpression.getInitialiser()
                && terminator == forExpression.getTerminator()
                && step == forExpression.getStep()
                && range == forExpression.getRange()
                && body == forExpression.getBody()) {
            return forExpression;
        }

        return new BoundForExpression(forExpression.getIterator(), initialiser, terminator, step, range, body);
    }

    protected BoundExpression rewriteVariableDeclaration(BoundVariableDeclarationExpression boundVariableDeclarationExpression) {

        BoundExpression initialiser = rewriteExpression(boundVariableDeclarationExpression.getInitialiser());
        BoundExpression range = boundVariableDeclarationExpression.getRange() == null
                ? null
                : rewriteExpression(boundVariableDeclarationExpression.getRange());

        if (boundVariableDeclarationExpression.isReadOnly()) {
            return new BoundConstDeclarationExpression(
                    boundVariableDeclarationExpression.getVariable(),
                    new BoundLiteralExpression(calculateConstant(boundVariableDeclarationExpression.getInitialiser()))
            );
        }

        if (initialiser == boundVariableDeclarationExpression.getInitialiser() && range == boundVariableDeclarationExpression.getRange()) {
            return boundVariableDeclarationExpression;
        }

        //TODO: The following three are more of the conditional's problem, not the assignments, since this should be the default behaviour

        //var x = if (cond) 1 else 2
        //This flattens this bad boy out into this:
        //conditional goto check
        //assignment to 2 (false) (was literal 2)
        //goto end
        //check label
        //assignment to 1 (true)
        //end label
        if (initialiser instanceof BoundBlockExpression) {
            List<BoundExpression> expressions = ((BoundBlockExpression)initialiser).getExpressions();

            for (int i = 0; i < expressions.size(); i++) {
                BoundExpression expression = expressions.get(i);
                if (expression instanceof BoundLiteralExpression) {
                    expressions.set(i, new BoundVariableDeclarationExpression(boundVariableDeclarationExpression.getVariable(), boundVariableDeclarationExpression.getRange(), expression, boundVariableDeclarationExpression.isReadOnly()));
                }
            }
            return new BoundBlockExpression(expressions);
        }

        //Now what
        else if (initialiser instanceof BoundForExpression) {

        }
        else if (initialiser instanceof BoundWhileExpression) {

        }

        return new BoundVariableDeclarationExpression(boundVariableDeclarationExpression.getVariable(), range, initialiser, boundVariableDeclarationExpression.isReadOnly());
    }

    protected BoundExpression rewriteWhileExpression(BoundWhileExpression boundWhileExpression) {

        BoundExpression condition = rewriteExpression(boundWhileExpression.getCondition());
        BoundExpression body = rewriteBlockExpression(boundWhileExpression.getBody());

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
        return new BoundWhileExpression(condition, (BoundBlockExpression) body);
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


    protected BoundExpression rewriteTypeofIntrinsic(BoundTypeofExpression typeofExpression) {

        BoundExpression expression = rewriteExpression(typeofExpression.getExpression());

        if (expression == typeofExpression.getExpression()) {
            return typeofExpression;
        }
        return new BoundTypeofExpression(typeofExpression);
    }

    protected BoundExpression rewritePrintIntrinsic(BoundPrintExpression printExpression) {

        BoundExpression expression = rewriteExpression(printExpression.getExpression());

        if (expression == printExpression.getExpression()) {
            return printExpression;
        }
        return new BoundPrintExpression(expression);
    }

}
