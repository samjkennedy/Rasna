package com.skennedy.bixbite.simulation;

import com.skennedy.bixbite.exceptions.UndefinedVariableException;
import com.skennedy.bixbite.exceptions.VariableAlreadyDeclaredException;
import com.skennedy.bixbite.exceptions.VariableOutsideRangeException;
import com.skennedy.bixbite.lowering.BoundConditionalGotoExpression;
import com.skennedy.bixbite.lowering.BoundGotoExpression;
import com.skennedy.bixbite.lowering.BoundLabel;
import com.skennedy.bixbite.lowering.BoundLabelExpression;
import com.skennedy.bixbite.typebinding.BoundAssignmentExpression;
import com.skennedy.bixbite.typebinding.BoundBinaryExpression;
import com.skennedy.bixbite.typebinding.BoundBinaryOperator;
import com.skennedy.bixbite.typebinding.BoundConstDeclarationExpression;
import com.skennedy.bixbite.typebinding.BoundExpression;
import com.skennedy.bixbite.typebinding.BoundLiteralExpression;
import com.skennedy.bixbite.typebinding.BoundPrintExpression;
import com.skennedy.bixbite.typebinding.BoundProgram;
import com.skennedy.bixbite.typebinding.BoundTypeofExpression;
import com.skennedy.bixbite.typebinding.BoundVariableDeclarationExpression;
import com.skennedy.bixbite.typebinding.BoundVariableExpression;
import com.skennedy.bixbite.typebinding.VariableSymbol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Simulator {

    private static final Logger log = LogManager.getLogger(Simulator.class);

    private Scope scope;
    private int ip;
    private Map<BoundLabel, Integer> labelToIp;

    private BoundProgram program;
    private PrintStream out;

    public Simulator() {
        this(System.out);
    }

    public Simulator(PrintStream out) {

        scope = new Scope(null);
        this.out = out;
    }

    public void simulate(BoundProgram program) {
        this.program = program;

        labelToIp = new HashMap<>();

        ip = 0;
        for (BoundExpression expression : program.getExpressions()) {
            if (expression instanceof BoundLabelExpression) {
                labelToIp.put(((BoundLabelExpression) expression).getLabel(), ip);
            }
            ip++;
        }
        ip = 0;

        while (ip < program.getExpressions().size()) {
            BoundExpression expression = program.getExpressions().get(ip);
            log.debug(ip + " - " + expression.getBoundExpressionType());
            evaluate(expression);
        }
    }

    private Object evaluate(BoundExpression expression) {

        Object res = null;
        switch (expression.getBoundExpressionType()) {
            case LITERAL:
                return  ((BoundLiteralExpression) expression).getValue();
            case VARIABLE_EXPRESSION:
                return evaluateVariableExpression((BoundVariableExpression) expression);
            case VARIABLE_DECLARATION:
                if (expression instanceof BoundConstDeclarationExpression) {
                    return evaluate((BoundConstDeclarationExpression) expression);
                }
                return evaluate((BoundVariableDeclarationExpression) expression);
            case BINARY_EXPRESSION:
                return evaluateBinaryExpression((BoundBinaryExpression) expression);
            case PRINT_INTRINSIC:
                ip++;
                evaluatePrintExpression((BoundPrintExpression) expression);
                return null;
            case TYPEOF_INTRINSIC:
                return evaluateTypeofExpression((BoundTypeofExpression) expression);
            case CONDITIONAL_GOTO:
                BoundConditionalGotoExpression conditionalGotoExpression = (BoundConditionalGotoExpression) expression;
                boolean condition = (boolean) evaluate(conditionalGotoExpression.getCondition());
                if ((condition && !conditionalGotoExpression.jumpIfFalse()) || (!condition && conditionalGotoExpression.jumpIfFalse())) {
                    ip = labelToIp.get(conditionalGotoExpression.getLabel());
                } else {
                    ip++;
                }
                if (ip >= program.getExpressions().size()) {
                    return res;
                }
                return evaluate(program.getExpressions().get(ip));
            case GOTO:
                ip = labelToIp.get(((BoundGotoExpression) expression).getLabel());
                return res;
            case ASSIGNMENT_EXPRESSION:
                return evaluate((BoundAssignmentExpression) expression);
            case NOOP:
            case LABEL:
                ip++;
                return res;
            default:
                throw new IllegalStateException("Unexpected value: " + expression.getBoundExpressionType());
        }
    }

    private Object evaluate(BoundAssignmentExpression assignmentExpression) {

        VariableSymbol variable = assignmentExpression.getVariable();
        if (!scope.tryLookup(variable.getName()).isPresent()) {
            throw new UndefinedVariableException(variable.getName());
        }
        Object value = evaluate(assignmentExpression.getExpression());

        scope.reassignVariable(variable.getName(), value);

        if (assignmentExpression.getRange() != null) {
            boolean withinRange = (boolean) evaluate(assignmentExpression.getRange());
            if (!withinRange) {
                throw new VariableOutsideRangeException(variable.getName());
            }
        }

        ip++;
        return value;
    }

    private Object evaluate(BoundVariableDeclarationExpression variableDeclarationExpression) {

        VariableSymbol variable = variableDeclarationExpression.getVariable();
        if (scope.tryLookup(variable.getName()).isPresent()) {
            throw new VariableAlreadyDeclaredException(variable.getName());
        }

        Object value = evaluate(variableDeclarationExpression.getInitialiser());

        scope.declareVariable(variable.getName(), value);

        if (variableDeclarationExpression.getRange() != null) {
            boolean withinRange = (boolean) evaluate(variableDeclarationExpression.getRange());
            if (!withinRange) {
                throw new VariableOutsideRangeException(variable.getName());
            }
        }

        ip++;
        return value;
    }

    private Object evaluate(BoundConstDeclarationExpression constDeclarationExpression) {

        VariableSymbol variable = constDeclarationExpression.getVariable();
        if (scope.tryLookup(variable.getName()).isPresent()) {
            throw new VariableAlreadyDeclaredException(variable.getName());
        }

        Object value = constDeclarationExpression.getConstValue().getValue();
        if (value instanceof BoundLiteralExpression) {
            value = ((BoundLiteralExpression) value).getValue();
        }
        scope.declareVariable(variable.getName(), value);

        ip++;
        return value;
    }

    private void evaluatePrintExpression(BoundPrintExpression printExpression) {

        Object value = evaluate(printExpression.getExpression());
        out.println(value);
    }

    private String evaluateTypeofExpression(BoundTypeofExpression typeofExpression) {

        ip++;
        return typeofExpression.getExpression().getType().getName();
    }

    private Object evaluateVariableExpression(BoundVariableExpression boundVariableExpression) {
        return scope.tryLookup(boundVariableExpression.getVariable().getName()).orElse(null);
    }

    private Object evaluateBinaryExpression(BoundBinaryExpression expression) {
        if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.ADDITION) {
            return (int) evaluate(expression.getLeft()) + (int) evaluate(expression.getRight());
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.SUBTRACTION) {
            return (int) evaluate(expression.getLeft()) - (int) evaluate(expression.getRight());
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.MULTIPLICATION) {
            return (int) evaluate(expression.getLeft()) * (int) evaluate(expression.getRight());
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.DIVISION) {
            return (int) evaluate(expression.getLeft()) / (int) evaluate(expression.getRight());
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.REMAINDER) {
            return (int) evaluate(expression.getLeft()) % (int) evaluate(expression.getRight());
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.EQUALS) {
            return (int) evaluate(expression.getLeft()) == (int) evaluate(expression.getRight());
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.NOT_EQUALS) {
            return (int) evaluate(expression.getLeft()) != (int) evaluate(expression.getRight());
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.GREATER_THAN) {
            return (int) evaluate(expression.getLeft()) > (int) evaluate(expression.getRight());
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.LESS_THAN) {
            return (int) evaluate(expression.getLeft()) < (int) evaluate(expression.getRight());
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.GREATER_THAN_OR_EQUAL) {
            return (int) evaluate(expression.getLeft()) >= (int) evaluate(expression.getRight());
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.LESS_THAN_OR_EQUAL) {
            return (int) evaluate(expression.getLeft()) <= (int) evaluate(expression.getRight());
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.BOOLEAN_AND) {
            return (boolean) evaluate(expression.getLeft()) && (boolean) evaluate(expression.getRight());
        } else if (expression.getOperator().getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.BOOLEAN_OR) {
            return (boolean) evaluate(expression.getLeft()) || (boolean) evaluate(expression.getRight());
        }
        throw new IllegalStateException("Unexpected value: " + expression.getOperator().getBoundOpType());
    }

    private static class Scope {

        private Scope parentScope;
        private Map<String, Object> variables;

        public Scope(Scope parentScope) {
            this.parentScope = parentScope;
            this.variables = new HashMap<>();
        }

        public Optional<Object> tryLookup(String name) {

            if (variables.containsKey(name)) {
                return Optional.of(variables.get(name));
            }
            if (parentScope != null) {
                return parentScope.tryLookup(name);
            }
            return Optional.empty();
        }

        public void declareVariable(String name, Object variable) {
            if (tryLookup(name).isPresent()) {
                throw new VariableAlreadyDeclaredException(name);
            }
            variables.put(name, variable);
        }

        public void reassignVariable(String name, Object variable) {
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
