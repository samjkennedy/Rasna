package com.skennedy.lazuli.simulation;

import com.skennedy.lazuli.exceptions.FunctionAlreadyDeclaredException;
import com.skennedy.lazuli.exceptions.UndefinedFunctionException;
import com.skennedy.lazuli.exceptions.UndefinedVariableException;
import com.skennedy.lazuli.exceptions.VariableAlreadyDeclaredException;
import com.skennedy.lazuli.exceptions.VariableOutsideRangeException;
import com.skennedy.lazuli.lowering.BoundArrayLengthExpression;
import com.skennedy.lazuli.lowering.BoundConditionalGotoExpression;
import com.skennedy.lazuli.lowering.BoundGotoExpression;
import com.skennedy.lazuli.lowering.BoundLabel;
import com.skennedy.lazuli.lowering.BoundLabelExpression;
import com.skennedy.lazuli.lowering.BoundNoOpExpression;
import com.skennedy.lazuli.typebinding.BoundArrayAccessExpression;
import com.skennedy.lazuli.typebinding.BoundArrayLiteralExpression;
import com.skennedy.lazuli.typebinding.BoundAssignmentExpression;
import com.skennedy.lazuli.typebinding.BoundBinaryExpression;
import com.skennedy.lazuli.typebinding.BoundBinaryOperator;
import com.skennedy.lazuli.typebinding.BoundConstDeclarationExpression;
import com.skennedy.lazuli.typebinding.BoundExpression;
import com.skennedy.lazuli.typebinding.BoundFunctionArgumentExpression;
import com.skennedy.lazuli.typebinding.BoundFunctionCallExpression;
import com.skennedy.lazuli.typebinding.BoundFunctionDeclarationExpression;
import com.skennedy.lazuli.typebinding.BoundLiteralExpression;
import com.skennedy.lazuli.typebinding.BoundPrintExpression;
import com.skennedy.lazuli.typebinding.BoundProgram;
import com.skennedy.lazuli.typebinding.BoundReturnExpression;
import com.skennedy.lazuli.typebinding.BoundTypeofExpression;
import com.skennedy.lazuli.typebinding.BoundVariableDeclarationExpression;
import com.skennedy.lazuli.typebinding.BoundVariableExpression;
import com.skennedy.lazuli.typebinding.FunctionSymbol;
import com.skennedy.lazuli.typebinding.TypeSymbol;
import com.skennedy.lazuli.typebinding.VariableSymbol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

public class Simulator {

    private static final Logger log = LogManager.getLogger(Simulator.class);

    private Scope scope;
    private int ip;
    private Map<BoundLabel, Integer> labelToIp;

    private Stack<Integer> returnStack;
    private Stack<Object> localsStack;

    private BoundProgram program;
    private PrintStream out;

    public Simulator() {
        this(System.out);
    }

    public Simulator(PrintStream out) {

        scope = new Scope(null);
        returnStack = new Stack<>();
        localsStack = new Stack<>();
        this.out = out;
    }

    public void simulate(BoundProgram program) {
        this.program = program;

        labelToIp = new HashMap<>();


        //Forward declare all methods
        List<BoundExpression> expressions = program.getExpressions();

        for (int i = 0; i < expressions.size(); ) {
            BoundExpression expression = expressions.get(i);
            if (expression instanceof BoundFunctionDeclarationExpression) {
                BoundFunctionDeclarationExpression functionDeclarationExpression = (BoundFunctionDeclarationExpression) expression;

                scope.declareFunction(functionDeclarationExpression.getFunctionSymbol(), i);

                program.getExpressions().remove(i);
                if (functionDeclarationExpression.getFunctionSymbol().getType() == TypeSymbol.VOID) {
                    program.getExpressions().add(i, new BoundReturnExpression(new BoundNoOpExpression()));
                }
                program.getExpressions().addAll(i, functionDeclarationExpression.getBody().getExpressions());
                if (functionDeclarationExpression.getFunctionSymbol().getType() == TypeSymbol.VOID) {
                    i += 1;
                }

                i += ((BoundFunctionDeclarationExpression) expression).getBody().getExpressions().size();
                ip = i;
            } else {
                i++;
            }
        }
        int instructionStart = ip;

        ip = 0;
        for (BoundExpression expression : program.getExpressions()) {
            if (expression instanceof BoundLabelExpression) {
                labelToIp.put(((BoundLabelExpression) expression).getLabel(), ip);
            }
            ip++;
        }
        int programEnd = ip;
        ip = instructionStart;

        while (ip < programEnd) {
            BoundExpression expression = program.getExpressions().get(ip);
            log.debug(ip + " - " + expression.getBoundExpressionType());
            evaluate(expression);
            ip++;
        }
    }

    private void evaluate(BoundExpression expression) {

        switch (expression.getBoundExpressionType()) {
            case ARRAY_LITERAL_EXPRESSION:
                evaluateArrayLiteralExpression((BoundArrayLiteralExpression) expression);
                break;
            case ARRAY_ACCESS_EXPRESSION:
                evaluateArrayAccessExpression((BoundArrayAccessExpression) expression);
                break;
            case ARRAY_LENGTH_EXPRESSION:
                evaluateArrayLengthExpression((BoundArrayLengthExpression) expression);
                break;
            case LITERAL:
                localsStack.push(((BoundLiteralExpression) expression).getValue());
                break;
            case VARIABLE_EXPRESSION:
                evaluateVariableExpression((BoundVariableExpression) expression);
                break;
            case VARIABLE_DECLARATION:
                if (expression instanceof BoundConstDeclarationExpression) {
                    evaluate((BoundConstDeclarationExpression) expression);
                } else {
                    evaluate((BoundVariableDeclarationExpression) expression);
                }
                break;
            case BINARY_EXPRESSION:
                evaluateBinaryExpression((BoundBinaryExpression) expression);
                break;
            case PRINT_INTRINSIC:
                evaluatePrintExpression((BoundPrintExpression) expression);
                break;
            case TYPEOF_INTRINSIC:
                evaluateTypeofExpression((BoundTypeofExpression) expression);
                break;
            case CONDITIONAL_GOTO:
                BoundConditionalGotoExpression conditionalGotoExpression = (BoundConditionalGotoExpression) expression;
                evaluate(conditionalGotoExpression.getCondition());

                boolean condition = (boolean) localsStack.pop();

                if ((condition && !conditionalGotoExpression.jumpIfFalse()) || (!condition && conditionalGotoExpression.jumpIfFalse())) {
                    ip = labelToIp.get(conditionalGotoExpression.getLabel());
                }
                if (ip >= program.getExpressions().size()) {
                    return;
                }
                break;
            case GOTO:
                ip = labelToIp.get(((BoundGotoExpression) expression).getLabel());
                break;
            case ASSIGNMENT_EXPRESSION:
                evaluate((BoundAssignmentExpression) expression);
                break;
            case NOOP:
            case LABEL:
                break;
            case FUNCTION_CALL:
                evaluateFunctionCall((BoundFunctionCallExpression) expression);
                break;
            case RETURN:
                evaluateReturn((BoundReturnExpression) expression);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + expression.getBoundExpressionType());
        }
    }

    private void evaluateReturn(BoundReturnExpression returnExpression) {
        evaluate(returnExpression.getReturnValue());
        ip = returnStack.pop();
    }

    private void evaluateFunctionCall(BoundFunctionCallExpression functionCallExpression) {

        Optional<Integer> methodIp = scope.tryLookupFunction(functionCallExpression.getFunction());
        if (methodIp.isEmpty()) {
            throw new UndefinedFunctionException(functionCallExpression.getFunction().getName());
        }

        Scope returnScope = scope;
        scope = new Scope(null);
        scope.functionsIps = returnScope.functionsIps;

        List<BoundExpression> argumentInitialisers = functionCallExpression.getBoundArguments();
        List<BoundFunctionArgumentExpression> arguments = functionCallExpression.getFunction().getArguments();
        for (int arg = 0; arg < argumentInitialisers.size(); arg++) {
            BoundFunctionArgumentExpression argument = arguments.get(arg);
            BoundExpression initialiser = argumentInitialisers.get(arg);
            BoundVariableDeclarationExpression variableDeclarationExpression = new BoundVariableDeclarationExpression(
                    argument.getArgument(),
                    argument.getRange(),
                    initialiser,
                    argument.getArgument().isReadOnly()
            );
            evaluate(variableDeclarationExpression);
        }
        returnStack.push(ip);

        ip = methodIp.get();

        BoundExpression expression;
        do {
            expression = program.getExpressions().get(ip);
            evaluate(expression);
            ip++;
            //This happens if the method call was the last instruction
            if (ip >= program.getExpressions().size()) {
                return;
            }
        } while (!(expression instanceof BoundReturnExpression));
        ip--;

        scope = returnScope;
    }

    private void evaluateArrayLiteralExpression(BoundArrayLiteralExpression arrayLiteralExpression) {
        Object[] array = new Object[arrayLiteralExpression.getElements().size()];


        for (int i = 0; i < arrayLiteralExpression.getElements().size(); i++) {
            evaluate(arrayLiteralExpression.getElements().get(i));
            array[i] = localsStack.pop();
        }
        //TODO: To really mimic the JVM push a reference
        localsStack.push(new LazuliArray<>(array));
    }

    private void evaluateArrayAccessExpression(BoundArrayAccessExpression arrayAccessExpression) {
        evaluate(arrayAccessExpression.getArray());
        evaluate(arrayAccessExpression.getIndex());
        int index = (int) localsStack.pop();
        LazuliArray<Object> array = (LazuliArray<Object>) localsStack.pop();
        localsStack.push(array.get(index));
    }

    private void evaluateArrayLengthExpression(BoundArrayLengthExpression arrayLengthExpression) {
        evaluate(arrayLengthExpression.getIterable());
        LazuliArray lazuliArray = (LazuliArray) localsStack.pop();
        localsStack.push(lazuliArray.array.length);
    }

    private class LazuliArray<T> {

        private T[] array;

        LazuliArray(T[] array) {
            this.array = array;
        }

        public T get(int index) {
            return array[index];
        }

        public T set(int index, T value) {
            array[index] = value;
            return value;
        }

        public T[] getArray() {
            return array;
        }

        public void setArray(T[] array) {
            this.array = array;
        }

        @Override
        public String toString() {
            return "[" + Arrays.stream(array).map(Object::toString).collect(Collectors.joining(", ")) + "]";
        }
    }

    private void evaluate(BoundAssignmentExpression assignmentExpression) {

        VariableSymbol variable = assignmentExpression.getVariable();
        if (scope.tryLookupVariable(variable.getName()).isEmpty()) {
            throw new UndefinedVariableException(variable.getName());
        }
        evaluate(assignmentExpression.getExpression());
        Object value = localsStack.pop();

        scope.reassignVariable(variable.getName(), value);

        if (assignmentExpression.getRange() != null) {
            evaluate(assignmentExpression.getRange());
            boolean withinRange = (boolean) localsStack.pop();
            if (!withinRange) {
                throw new VariableOutsideRangeException(variable.getName());
            }
        }
        localsStack.push(value);
    }

    private void evaluate(BoundVariableDeclarationExpression variableDeclarationExpression) {

        VariableSymbol variable = variableDeclarationExpression.getVariable();
        if (scope.tryLookupVariable(variable.getName()).isPresent()) {
            throw new VariableAlreadyDeclaredException(variable.getName());
        }

        Object value = 0; //TODO: default primitive values
        if (variableDeclarationExpression.getInitialiser() != null) {
            evaluate(variableDeclarationExpression.getInitialiser());
            value = localsStack.pop();
        }
        scope.declareVariable(variable.getName(), value);

        if (variableDeclarationExpression.getRange() != null) {
            evaluate(variableDeclarationExpression.getRange());
            boolean withinRange = (boolean) localsStack.pop();
            if (!withinRange) {
                throw new VariableOutsideRangeException(variable.getName());
            }
        }
        localsStack.push(value);
    }

    private void evaluate(BoundConstDeclarationExpression constDeclarationExpression) {

        VariableSymbol variable = constDeclarationExpression.getVariable();
        if (scope.tryLookupVariable(variable.getName()).isPresent()) {
            throw new VariableAlreadyDeclaredException(variable.getName());
        }

        Object value = constDeclarationExpression.getConstValue().getValue();
        if (value instanceof BoundLiteralExpression) {
            value = ((BoundLiteralExpression) value).getValue();
        }
        scope.declareVariable(variable.getName(), value);
        localsStack.push(value);
    }

    private void evaluatePrintExpression(BoundPrintExpression printExpression) {

        evaluate(printExpression.getExpression());
        out.println(localsStack.pop());
    }

    private void evaluateTypeofExpression(BoundTypeofExpression typeofExpression) {
        //TODO: push a String reference
        localsStack.push(typeofExpression.getExpression().getType().getName());
    }

    private void evaluateVariableExpression(BoundVariableExpression boundVariableExpression) {
        Optional<Object> value = scope.tryLookupVariable(boundVariableExpression.getVariable().getName());
        if (value.isEmpty()) {
            throw new UndefinedVariableException(boundVariableExpression.getVariable().getName());
        }
        localsStack.push(value.get());
    }

    private void evaluateBinaryExpression(BoundBinaryExpression expression) {
        evaluate(expression.getLeft());
        Object left = localsStack.pop();
        evaluate(expression.getRight());
        Object right = localsStack.pop();
        if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.ADDITION) {
            localsStack.push((int) left + (int) right);
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.SUBTRACTION) {
            localsStack.push((int) left - (int) right);
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.MULTIPLICATION) {
            localsStack.push((int) left * (int) right);
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.DIVISION) {
            localsStack.push((int) left / (int) right);
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.REMAINDER) {
            localsStack.push((int) left % (int) right);
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.EQUALS) {
            localsStack.push((int) left == (int) right);
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.NOT_EQUALS) {
            localsStack.push((int) left != (int) right);
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.GREATER_THAN) {
            localsStack.push((int) left > (int) right);
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.LESS_THAN) {
            localsStack.push((int) left < (int) right);
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.GREATER_THAN_OR_EQUAL) {
            localsStack.push((int) left >= (int) right);
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.LESS_THAN_OR_EQUAL) {
            localsStack.push((int) left <= (int) right);
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.BOOLEAN_AND) {
            localsStack.push((boolean) left && (boolean) right);
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.BOOLEAN_OR) {
            localsStack.push((boolean) left || (boolean) right);
        } else {
            throw new IllegalStateException("Unexpected value: " + expression.getOperator().getBoundOpType());
        }
    }

    private static class Scope {

        private Scope parentScope;
        private Map<String, Object> variables;
        private Map<FunctionSymbol, Integer> functionsIps;

        Scope(Scope parentScope) {
            this.parentScope = parentScope;
            this.variables = new HashMap<>();
            this.functionsIps = new HashMap<>();
        }

        Optional<Object> tryLookupVariable(String name) {

            if (variables.containsKey(name)) {
                return Optional.ofNullable(variables.get(name));
            }
            if (parentScope != null) {
                return parentScope.tryLookupVariable(name);
            }
            return Optional.empty();
        }

        Optional<Integer> tryLookupFunction(FunctionSymbol function) {

            if (functionsIps.containsKey(function)) {
                return Optional.ofNullable(functionsIps.get(function));
            }
            if (parentScope != null) {
                return parentScope.tryLookupFunction(function);
            }
            return Optional.empty();
        }

        void declareVariable(String name, Object variable) {
            if (tryLookupVariable(name).isPresent()) {
                throw new VariableAlreadyDeclaredException(name);
            }
            variables.put(name, variable);
        }

        void declareFunction(FunctionSymbol function, Integer ip) {
            if (tryLookupFunction(function).isPresent()) {
                //TODO: Get args
                throw new FunctionAlreadyDeclaredException(function.getName());
            }
            functionsIps.put(function, ip);
        }

        void reassignVariable(String name, Object variable) {
            if (!variables.containsKey(name)) {
                if (parentScope == null) {
                    throw new UndefinedVariableException(name);
                }
                parentScope.reassignVariable(name, variable);
            }
            variables.replace(name, variable);
        }
    }
}
