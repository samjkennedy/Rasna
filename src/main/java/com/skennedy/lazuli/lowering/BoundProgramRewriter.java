package com.skennedy.lazuli.lowering;

import com.skennedy.lazuli.parsing.model.OpType;
import com.skennedy.lazuli.typebinding.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
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
            case POSITIONAL_ACCESS_EXPRESSION:
                return rewriteArrayAccessExpression((BoundPositionalAccessExpression) expression);
            case ARRAY_ASSIGNMENT_EXPRESSION:
                return rewriteArrayAssignmentExpression((BoundArrayAssignmentExpression) expression);
            case MEMBER_ASSIGNMENT_EXPRESSION:
                return rewriteMemberAssignmentExpression((BoundMemberAssignmentExpression) expression);
            case ARRAY_LENGTH_EXPRESSION:
            case TUPLE_LITERAL_EXPRESSION:
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
            case LAMBDA_FUNCTION:
                return rewriteLambdaExpression((BoundLambdaExpression) expression);
            case MAP_EXPRESSION:
                return rewriteMapExpression((BoundMapExpression) expression);
            case ARRAY_DECLARATION_EXPRESSION:
                return rewriteArrayDeclarationExpression((BoundArrayDeclarationExpression) expression);
            case YIELD_EXPRESSION:
                return rewriteYieldExpression((BoundYieldExpression) expression);
            case STRUCT_DECLARATION_EXPRESSION:
                return rewriteStructDeclarationExpression((BoundStructDeclarationExpression) expression);
            case STRUCT_LITERAL_EXPRESSION:
                return rewriteStructLiteralExpression((BoundStructLiteralExpression) expression);
            case MEMBER_ACCESSOR:
                return rewriteMemberAccessorExpression((BoundMemberAccessorExpression) expression);
            case CAST_EXPRESSION:
                return rewriteCastExpression((BoundCastExpression) expression);
            default:
                throw new IllegalStateException("Unexpected value: " + expression.getBoundExpressionType());
        }
    }

    private BoundExpression rewriteCastExpression(BoundCastExpression castExpression) {
        BoundExpression rewrittenExpression = rewriteExpression(castExpression.getExpression());

        if (rewrittenExpression == castExpression.getExpression()) {
            return castExpression;
        }
        return new BoundCastExpression(rewrittenExpression, castExpression.getType());
    }

    private BoundExpression rewriteMemberAccessorExpression(BoundMemberAccessorExpression memberAccessorExpression) {

        BoundExpression owner = rewriteExpression(memberAccessorExpression.getOwner());

        if (owner == memberAccessorExpression.getOwner()) {
            return memberAccessorExpression;
        }

        return new BoundMemberAccessorExpression(owner, memberAccessorExpression.getMember());
    }

    protected BoundExpression rewriteStructDeclarationExpression(BoundStructDeclarationExpression structDeclarationExpression) {

        List<BoundExpression> members = new ArrayList<>();
        for (BoundExpression member : structDeclarationExpression.getMembers()) {
            members.add(rewriteExpression(member));
        }

        if (members == structDeclarationExpression.getMembers()) {
            return structDeclarationExpression;
        }

        return new BoundStructDeclarationExpression(structDeclarationExpression.getType(), members);
    }

    private BoundExpression rewriteYieldExpression(BoundYieldExpression yieldExpression) {
        BoundExpression rewrittenExpression = rewriteExpression(yieldExpression.getExpression());

        if (rewrittenExpression == yieldExpression.getExpression()) {
            return yieldExpression;
        }
        return new BoundYieldExpression(rewrittenExpression);
    }

    private BoundExpression rewriteArrayDeclarationExpression(BoundArrayDeclarationExpression arrayDeclarationExpression) {
        BoundExpression rewrittenElementCount = rewriteExpression(arrayDeclarationExpression.getElementCount());

        if (rewrittenElementCount == arrayDeclarationExpression.getElementCount()) {
            return arrayDeclarationExpression;
        }
        return new BoundArrayDeclarationExpression((ArrayTypeSymbol) arrayDeclarationExpression.getType(), rewrittenElementCount);
    }

    protected BoundExpression rewriteMapExpression(BoundMapExpression mapExpression) {

        BoundExpression rewrittenMapper = rewriteExpression(mapExpression.getMapperFunction());
        BoundExpression rewrittenOperand = rewriteExpression(mapExpression.getOperand());

        if (rewrittenMapper == mapExpression.getMapperFunction() && rewrittenOperand == mapExpression.getOperand()) {
            return mapExpression;
        }
        return new BoundMapExpression(rewrittenMapper, rewrittenOperand);
    }

    private BoundExpression rewriteLambdaExpression(BoundLambdaExpression lambdaExpression) {
        BoundExpression rewrittenBody = rewriteExpression(lambdaExpression.getBody());

        if (rewrittenBody == lambdaExpression.getBody()) {
            return lambdaExpression;
        }
        return new BoundLambdaExpression(lambdaExpression.getArguments(), rewrittenBody);
    }

    protected BoundExpression rewriteMatchExpression(BoundMatchExpression matchExpression) {

        if (matchExpression.getMatchCaseExpressions().isEmpty()) {
            return new BoundNoOpExpression();
        }

        BoundExpression rewrittenOperand = rewriteExpression(matchExpression.getOperand());

        List<BoundMatchCaseExpression> rewrittenCaseExpressions = new ArrayList<>();
        for (BoundMatchCaseExpression caseExpression : matchExpression.getMatchCaseExpressions()) {
            rewrittenCaseExpressions.add(rewriteMatchCaseExpression(caseExpression, matchExpression.getOperand()));
        }

        if (rewrittenCaseExpressions != matchExpression.getMatchCaseExpressions()
                || rewrittenOperand != matchExpression.getOperand()) {
            return new BoundMatchExpression(matchExpression.getType(), rewrittenOperand, rewrittenCaseExpressions);
        }
        return matchExpression;
    }

    protected BoundMatchCaseExpression rewriteMatchCaseExpression(BoundMatchCaseExpression matchCaseExpression, BoundExpression operand) {
        BoundExpression rewrittenCaseExpression = null;
        if (matchCaseExpression.getCaseExpression() != null) {
            rewrittenCaseExpression = rewriteExpression(matchCaseExpression.getCaseExpression());
        }
        BoundExpression rewrittenThenExpression = rewriteExpression(matchCaseExpression.getThenExpression());

        if (rewrittenCaseExpression != matchCaseExpression.getCaseExpression()
                || rewrittenThenExpression != matchCaseExpression.getThenExpression()) {
            return new BoundMatchCaseExpression(rewrittenCaseExpression, rewrittenThenExpression);
        }

        if (rewrittenCaseExpression != null && rewrittenCaseExpression.getBoundExpressionType() == BoundExpressionType.VARIABLE_DECLARATION) {

            /*
            match v {
                s: String -> print(s),
                i: Int -> print(i),
                ...
            }

            becomes:

            match v {
                typeof(v) == String -> {
                    s: String = v as String
                    print(s)
                },
                typeof(v) == Int -> {
                    i: Int = v as Int
                    print(i)
                },
                ...
            }
             */
            BoundVariableDeclarationExpression variableDeclarationExpression = (BoundVariableDeclarationExpression) rewrittenCaseExpression;

            BoundExpression thenExpression = new BoundBlockExpression(
                    new BoundVariableDeclarationExpression(
                            variableDeclarationExpression.getVariable(),
                            variableDeclarationExpression.getGuard(),
                            new BoundCastExpression(operand, variableDeclarationExpression.getType()),
                            true
                    ),
                    matchCaseExpression.getThenExpression()
            );

            //return new BoundMatchCaseExpression(new BoundBinaryExpression(new BoundTypeofExpression(operand), BoundBinaryOperator.bind(OpType.EQ, TypeSymbol.TYPE, TypeSymbol.TYPE), new BoundTypeExpression()), thenExpression);

            throw new UnsupportedOperationException("Type match expressions are not yet implemented");
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
            BoundExpression rewrittenArg = rewriteExpression(arg);
            rewrittenArgs.add(rewrittenArg);
        }

        if (rewrittenArgs != functionCallExpression.getBoundArguments()) {
            return new BoundFunctionCallExpression(functionCallExpression.getFunction(), rewrittenArgs);
        }
        return functionCallExpression;
    }

    private BoundExpression rewriteStructLiteralExpression(BoundStructLiteralExpression structLiteralExpression) {

        List<BoundExpression> rewrittenElements = new ArrayList<>();

        boolean rewritten = false;
        for (BoundExpression element : structLiteralExpression.getElements()) {

            BoundExpression rewrittenElement = rewriteExpression(element);
            rewrittenElements.add(rewrittenElement);
            if (rewrittenElement != element) {
                rewritten = true;
            }
        }

        if (rewritten) {
            return new BoundStructLiteralExpression(structLiteralExpression.getType(), rewrittenElements);
        }
        return structLiteralExpression;
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

    private BoundExpression rewriteArrayAccessExpression(BoundPositionalAccessExpression arrayAccessExpression) {

        BoundExpression index = rewriteExpression(arrayAccessExpression.getIndex());

        if (index == arrayAccessExpression.getIndex()) {
            return arrayAccessExpression;
        }
        return new BoundPositionalAccessExpression(arrayAccessExpression.getArray(), index);
    }

    private BoundExpression rewriteArrayAssignmentExpression(BoundArrayAssignmentExpression arrayAssignmentExpression) {

        BoundExpression arrayAccessExpression = rewriteExpression(arrayAssignmentExpression.getArrayAccessExpression());
        BoundExpression assignment = rewriteExpression(arrayAssignmentExpression.getAssignment());

        if (arrayAccessExpression == arrayAssignmentExpression.getArrayAccessExpression()
                && assignment == arrayAssignmentExpression.getAssignment()) {
            return arrayAssignmentExpression;
        }
        return new BoundArrayAssignmentExpression(arrayAssignmentExpression.getArrayAccessExpression(), arrayAssignmentExpression.getAssignment());
    }

    private BoundExpression rewriteMemberAssignmentExpression(BoundMemberAssignmentExpression memberAssignmentExpression) {

        BoundExpression assignment = rewriteExpression(memberAssignmentExpression.getAssignment());

        if (assignment == memberAssignmentExpression.getAssignment()) {
            return memberAssignmentExpression;
        }
        return new BoundMemberAssignmentExpression(memberAssignmentExpression.getMemberAccessorExpression(), assignment);
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

                if (variableExpression.getType() == TypeSymbol.INT && (int) literalExpression.getValue() <= Byte.MAX_VALUE && (int) literalExpression.getValue() >= Byte.MIN_VALUE) {

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
                    (BoundBlockExpression) expression,
                    expr -> new BoundAssignmentExpression(assignmentExpression.getVariable(), assignmentExpression.getGuard(), expr));
        }
        return new BoundAssignmentExpression(assignmentExpression.getVariable(), assignmentExpression.getGuard(), assignmentExpression.getExpression());
    }

    protected BoundExpression rewriteBinaryExpression(BoundBinaryExpression boundBinaryExpression) {

        BoundExpression left = rewriteExpression(boundBinaryExpression.getLeft());
        BoundExpression right = rewriteExpression(boundBinaryExpression.getRight());

        //if both sides are constant, can do constant folding
//        if (left instanceof BoundLiteralExpression && right instanceof BoundLiteralExpression) {
//
//            return new BoundLiteralExpression(calculateConstant(boundBinaryExpression));
//        }

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
//        if (condition.getBoundExpressionType() == BoundExpressionType.LITERAL || condition.getBoundExpressionType() == BoundExpressionType.VARIABLE_EXPRESSION && ((BoundVariableExpression) condition).getVariable().isReadOnly()) {
//            boolean constValue = (boolean) calculateConstant(condition);
//            if (constValue) {
//                return body;
//            } else if (elseBody != null) {
//                return elseBody;
//            } else {
//                return new BoundNoOpExpression();
//            }
//        }

        if (condition == boundIfExpression.getCondition() && body == boundIfExpression.getBody() && elseBody == boundIfExpression.getElseBody()) {
            return boundIfExpression;
        }

        return new BoundIfExpression(condition, body, elseBody);
    }

    protected BoundExpression rewriteForExpression(BoundForExpression forExpression) {

        BoundRangeExpression range = rewriteRangeExpression(forExpression.getRangeExpression());
        BoundExpression guard = null;
        if (forExpression.getGuard() != null) {
            guard = rewriteExpression(forExpression.getGuard());
        }
        BoundExpression body = rewriteExpression(forExpression.getBody());

        if (body instanceof BoundNoOpExpression) {
            return new BoundNoOpExpression();
        }

        if (range == forExpression.getRangeExpression()
                && guard == forExpression.getGuard()
                && body == forExpression.getBody()) {
            return forExpression;
        }

        return new BoundForExpression(forExpression.getIterator(), range, guard, body);
    }

    private BoundRangeExpression rewriteRangeExpression(BoundRangeExpression rangeExpression) {

        BoundExpression lowerBound = rewriteExpression(rangeExpression.getLowerBound());
        BoundExpression upperBound = rewriteExpression(rangeExpression.getUpperBound());
        BoundExpression step = null;
        if (rangeExpression.getStep() != null) {
            step = rewriteExpression(rangeExpression.getStep());
        }

        if (lowerBound == rangeExpression.getLowerBound()
                && upperBound == rangeExpression.getUpperBound()
                && step == rangeExpression.getStep()
        ) {
            return rangeExpression;
        }
        return new BoundRangeExpression(lowerBound, upperBound, step);
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

//        if (boundVariableDeclarationExpression.isReadOnly() && boundVariableDeclarationExpression.getInitialiser() != null) {
//            return new BoundConstDeclarationExpression(
//                    boundVariableDeclarationExpression.getVariable(),
//                    boundVariableDeclarationExpression.getGuard(),
//                    boundVariableDeclarationExpression.getInitialiser(),
//                    new BoundLiteralExpression(calculateConstant(boundVariableDeclarationExpression.getInitialiser()))
//            );
//        }

        if (initialiser == boundVariableDeclarationExpression.getInitialiser() && guard == boundVariableDeclarationExpression.getGuard()) {
            return boundVariableDeclarationExpression;
        }

        if (initialiser instanceof BoundBlockExpression) {
            return rewriteBlockInitialiser(boundVariableDeclarationExpression, (BoundBlockExpression) initialiser);
        }
        return boundVariableDeclarationExpression;
    }

    private BoundExpression rewriteBlockInitialiser(BoundVariableDeclarationExpression boundVariableDeclarationExpression, BoundBlockExpression initialiser) {
        BoundExpression blockInitialiser;
        switch (boundVariableDeclarationExpression.getInitialiser().getBoundExpressionType()) {
            default:
                throw new UnsupportedOperationException("Assignment is not supported for expressions of type " + boundVariableDeclarationExpression.getInitialiser().getBoundExpressionType());
            case IF:
            case MATCH_EXPRESSION:
                blockInitialiser = rewriteBlockInitialiser(
                        initialiser,
                        expr -> new BoundAssignmentExpression(boundVariableDeclarationExpression.getVariable(), boundVariableDeclarationExpression.getGuard(), expr)
                );
                BoundVariableDeclarationExpression tempInit = new BoundVariableDeclarationExpression(boundVariableDeclarationExpression.getVariable(), boundVariableDeclarationExpression.getGuard(), new BoundLiteralExpression(getTypeDefaultValue(boundVariableDeclarationExpression.getVariable().getType())), false);

                return new BoundBlockExpression(
                        tempInit,
                        blockInitialiser
                );
            case FOR_IN: {
                BoundForInExpression forInExpression = (BoundForInExpression) boundVariableDeclarationExpression.getInitialiser();

                VariableSymbol iterationCounter = new VariableSymbol("iteration-counter-" + UUID.randomUUID().toString(), TypeSymbol.INT, null, false, null);
                BoundVariableExpression iterationCounterExpression = new BoundVariableExpression(iterationCounter);

                new BoundPositionalAccessExpression(forInExpression.getIterable(), iterationCounterExpression);

                List<BoundExpression> expressions = new ArrayList<>();
                expressions.addAll(Arrays.asList(
                        new BoundVariableDeclarationExpression(iterationCounter, null, new BoundLiteralExpression(0), false),
                        new BoundVariableDeclarationExpression(
                                boundVariableDeclarationExpression.getVariable(),
                                null,
                                forInExpression.getIterable(),
                                boundVariableDeclarationExpression.isReadOnly()
                        ),
                        rewriteBlockInitialiser(
                                initialiser,
                                expr -> new BoundBlockExpression(
                                        new BoundArrayAssignmentExpression(new BoundPositionalAccessExpression(forInExpression.getIterable(), iterationCounterExpression), expr),
                                        new BoundIncrementExpression(iterationCounter, new BoundLiteralExpression(1)
                                        ))
                        )));

                if (forInExpression.getGuard() != null) {

                    VariableSymbol copyIndex = new VariableSymbol("copy-index-" + UUID.randomUUID().toString(), TypeSymbol.INT, null, false, null);
                    BoundVariableExpression copyIndexExpression = new BoundVariableExpression(copyIndex);

                    VariableSymbol filteredArray = new VariableSymbol("filtered-array-" + UUID.randomUUID().toString(), boundVariableDeclarationExpression.getVariable().getType(), null, false, null);
                    BoundVariableExpression filteredArrayVariable = new BoundVariableExpression(filteredArray);
                    BoundVariableExpression arrayVariableExpression = new BoundVariableExpression(boundVariableDeclarationExpression.getVariable());

                    expressions.addAll(Arrays.asList(
                            //Create new array of index size
                            new BoundVariableDeclarationExpression(filteredArray, null, new BoundArrayDeclarationExpression((ArrayTypeSymbol) filteredArrayVariable.getType(), iterationCounterExpression), false),
                            //For each element copy to the array
                            rewriteForExpression(new BoundForExpression(copyIndex, new BoundRangeExpression(new BoundLiteralExpression(0), iterationCounterExpression, new BoundLiteralExpression(1)), null, new BoundBlockExpression(
                                    new BoundArrayAssignmentExpression(
                                            new BoundPositionalAccessExpression(filteredArrayVariable, copyIndexExpression),
                                            new BoundPositionalAccessExpression(arrayVariableExpression, copyIndexExpression)
                                    )
                            ))),
                            //Assign the filtered array to the original variable
                            new BoundAssignmentExpression(
                                    boundVariableDeclarationExpression.getVariable(),
                                    boundVariableDeclarationExpression.getGuard(),
                                    filteredArrayVariable
                            )
                    ));
                }

                return new BoundBlockExpression(expressions);
            }
            case FOR: {
                BoundForExpression forExpression = (BoundForExpression) boundVariableDeclarationExpression.getInitialiser();

                BoundExpression elementCount;
                BoundRangeExpression rangeExpression = forExpression.getRangeExpression();
                if (!(rangeExpression.getLowerBound() instanceof BoundLiteralExpression) || (int) ((BoundLiteralExpression) rangeExpression.getLowerBound()).getValue() != 0) {
                    elementCount = new BoundBinaryExpression(
                            rangeExpression.getUpperBound(),
                            BoundBinaryOperator.bind(OpType.SUB, TypeSymbol.INT, TypeSymbol.INT),
                            rangeExpression.getLowerBound()
                    );
                } else {
                    elementCount = rangeExpression.getUpperBound();
                }

                BoundArrayDeclarationExpression arrayDeclarationExpression = new BoundArrayDeclarationExpression(new ArrayTypeSymbol(boundVariableDeclarationExpression.getType()), elementCount);
                BoundVariableDeclarationExpression variableDeclarationExpression = new BoundVariableDeclarationExpression(
                        boundVariableDeclarationExpression.getVariable(),
                        boundVariableDeclarationExpression.getGuard(),
                        arrayDeclarationExpression,
                        boundVariableDeclarationExpression.isReadOnly()
                );

                //TODO: This can be optimised in the case where there is no guard or step - just use the in built array index

                VariableSymbol indexVariable = new VariableSymbol("index-" + UUID.randomUUID().toString(), TypeSymbol.INT, null, false, null);
                BoundVariableExpression indexExpression = new BoundVariableExpression(indexVariable);

                BoundExpression index;
                if (!(rangeExpression.getLowerBound() instanceof BoundLiteralExpression) || (int) ((BoundLiteralExpression) rangeExpression.getLowerBound()).getValue() != 0) {
                    index = new BoundBinaryExpression(
                            indexExpression,
                            BoundBinaryOperator.bind(OpType.SUB, TypeSymbol.INT, TypeSymbol.INT),
                            rangeExpression.getLowerBound()
                    );
                } else {
                    index = indexExpression;
                }

                BoundVariableExpression array = new BoundVariableExpression(boundVariableDeclarationExpression.getVariable());

                List<BoundExpression> expressions = new ArrayList<>();
                expressions.addAll(Arrays.asList(
                        variableDeclarationExpression,
                        new BoundVariableDeclarationExpression(indexVariable, null, rangeExpression.getLowerBound(), false),
                        rewriteBlockInitialiser(initialiser, expr -> new BoundBlockExpression(
                                new BoundArrayAssignmentExpression(new BoundPositionalAccessExpression(array, index), expr),
                                new BoundIncrementExpression(indexVariable, new BoundLiteralExpression(1))
                        ))
                ));

                if (forExpression.getGuard() != null || rangeExpression.getStep() != null) {

                    VariableSymbol copyIndex = new VariableSymbol("copy-index-" + UUID.randomUUID().toString(), TypeSymbol.INT, null, false, null);
                    BoundVariableExpression copyIndexExpression = new BoundVariableExpression(copyIndex);

                    VariableSymbol filteredArray = new VariableSymbol("filtered-array-" + UUID.randomUUID().toString(), boundVariableDeclarationExpression.getVariable().getType(), null, false, null);
                    BoundVariableExpression filteredArrayVariable = new BoundVariableExpression(filteredArray);
                    BoundVariableExpression arrayVariableExpression = new BoundVariableExpression(boundVariableDeclarationExpression.getVariable());

                    expressions.addAll(Arrays.asList(
                            //Create new array of index size
                            new BoundVariableDeclarationExpression(filteredArray, null, new BoundArrayDeclarationExpression((ArrayTypeSymbol) filteredArrayVariable.getType(), index), false),
                            //For each element copy to the array
                            rewriteForExpression(new BoundForExpression(copyIndex, new BoundRangeExpression(new BoundLiteralExpression(0), index, new BoundLiteralExpression(1)), null, new BoundBlockExpression(
                                    new BoundArrayAssignmentExpression(
                                            new BoundPositionalAccessExpression(filteredArrayVariable, copyIndexExpression),
                                            new BoundPositionalAccessExpression(arrayVariableExpression, copyIndexExpression)
                                    )
                            ))),
                            //Assign the filtered array to the original variable
                            new BoundAssignmentExpression(
                                    boundVariableDeclarationExpression.getVariable(),
                                    boundVariableDeclarationExpression.getGuard(),
                                    filteredArrayVariable
                            )
                    ));
                }
                return new BoundBlockExpression(expressions);
            }
        }
    }

    //TODO: This should be its own class perhaps
    private Object getTypeDefaultValue(TypeSymbol type) {
        if (type == TypeSymbol.INT) {
            return 0;
        }
        if (type == TypeSymbol.BOOL) {
            return false;
        }
        if (type == TypeSymbol.REAL) {
            return 0.0D;
        }
        if (type == TypeSymbol.STRING) {
            return "";
        }
        return null;
    }

    protected BoundExpression rewriteWhileExpression(BoundWhileExpression boundWhileExpression) {

        BoundExpression condition = rewriteExpression(boundWhileExpression.getCondition());
        BoundExpression body = rewriteExpression(boundWhileExpression.getBody());

        if (body instanceof BoundNoOpExpression) {
            return new BoundNoOpExpression();
        }

//        if (condition.getBoundExpressionType() == BoundExpressionType.LITERAL || condition.getBoundExpressionType() == BoundExpressionType.VARIABLE_EXPRESSION && ((BoundVariableExpression) condition).getVariable().isReadOnly()) {
//            boolean constValue = (boolean) calculateConstant(condition);
//            if (!constValue) {
//                return new BoundNoOpExpression();
//            } else {
//                throw new InfiniteLoopException();
//            }
//        }

        if (condition == boundWhileExpression.getCondition() && body == boundWhileExpression.getBody()) {
            return boundWhileExpression;
        }
        return new BoundWhileExpression(condition, body);
    }

//    private Object calculateConstant(BoundExpression expression) {
//        return expression;
//        switch (expression.getBoundExpressionType()) {
//
//            case LITERAL:
//                BoundLiteralExpression boundLiteralExpression = (BoundLiteralExpression) expression;
//                return boundLiteralExpression.getValue();
//            case VARIABLE_EXPRESSION:
//                return expression;
//            case BINARY_EXPRESSION:
//                BoundBinaryExpression boundBinaryExpression = (BoundBinaryExpression) expression;
//
//                Object left = calculateConstant(boundBinaryExpression.getLeft());
//                Object right = calculateConstant(boundBinaryExpression.getRight());
//
//                switch (boundBinaryExpression.getOperator().getOpType()) {
//
//                    case ADD:
//                        return (int)left + (int)right;
//                    case SUB:
//                        return (int)left - (int)right;
//                    case MUL:
//                    case DIV:
//                    case MOD:
//                    case INC:
//                    case EQ:
//                    case NEQ:
//                    case GT:
//                    case LT:
//                    case GTEQ:
//                    case LTEQ:
//                    case LAND:
//                    case LOR:
//                        break;
//                }
//
//                return expression; //TODO: Constant folding
//            //return calculateConstant(calculateConstant(boundBinaryExpression.getLeft()), calculateConstant(boundBinaryExpression.getRight()), boundBinaryExpression.getOperator());
//            default:
//                throw new IllegalStateException("Unexpected expression type for constant folding: " + expression.getBoundExpressionType());
//        }
//    }

    private BoundExpression rewriteConditionalGoto(BoundConditionalGotoExpression conditionalGotoExpression) {
        BoundExpression condition = rewriteExpression(conditionalGotoExpression.getCondition());
        if (condition == conditionalGotoExpression.getCondition()) {
            return conditionalGotoExpression;
        }
        return new BoundConditionalGotoExpression(conditionalGotoExpression.getLabel(), condition, conditionalGotoExpression.jumpIfFalse());
    }

//    private BoundExpression calculateConstant(Object left, Object right, BoundBinaryOperator operator) {
//        switch (operator.getBoundOpType()) {
//
//            case ADDITION:
//                return new BoundLiteralExpression((int) left + (int) right);
//            case SUBTRACTION:
//                return new BoundLiteralExpression((int) left - (int) right);
//            case MULTIPLICATION:
//                return new BoundLiteralExpression((int) left * (int) right);
//            case DIVISION:
//                return new BoundLiteralExpression((int) left / (int) right);
//            case REMAINDER:
//                return new BoundLiteralExpression((int) left % (int) right);
//            case GREATER_THAN:
//                return new BoundLiteralExpression((int) left > (int) right);
//            case LESS_THAN:
//                return new BoundLiteralExpression((int) left < (int) right);
//            case GREATER_THAN_OR_EQUAL:
//                return new BoundLiteralExpression((int) left >= (int) right);
//            case LESS_THAN_OR_EQUAL:
//                return new BoundLiteralExpression((int) left <= (int) right);
//            case EQUALS:
//                return new BoundLiteralExpression((int) left == (int) right);
//            case NOT_EQUALS:
//                return new BoundLiteralExpression((int) left != (int) right);
//            case BOOLEAN_OR:
//                return new BoundLiteralExpression((boolean) left || (boolean) right);
//            case BOOLEAN_AND:
//                return new BoundLiteralExpression((boolean) left && (boolean) right);
//            default:
//                throw new IllegalStateException("Unhandled bound binary operator for constant folding: " + operator.getBoundOpType());
//        }
//    }


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

    private static <T extends BoundExpression> BoundExpression rewriteBlockInitialiser(BoundBlockExpression initialiser, Function<BoundExpression, T> remapper) {
        List<BoundExpression> expressions = initialiser.getExpressions();

        for (int i = 0; i < expressions.size(); i++) {
            BoundExpression expr = expressions.get(i);
            if (expr instanceof BoundLiteralExpression
                    || expr instanceof BoundVariableExpression
                    || expr instanceof BoundBinaryExpression
                    || expr instanceof BoundFunctionCallExpression
                    || expr instanceof BoundYieldExpression) {
                expressions.set(i, remapper.apply(expr));
            } else if (expr instanceof BoundBlockExpression) {
                rewriteBlockInitialiser((BoundBlockExpression) expr, remapper);
            }
        }
        return new BoundBlockExpression(expressions);
    }

}
