package com.skennedy.rasna.typebinding;

import com.skennedy.rasna.exceptions.InvalidOperationException;
import com.skennedy.rasna.parsing.model.OpType;

import java.util.Collections;
import java.util.Iterator;

public class BoundUnaryOperator implements BoundExpression {

    private final OpType opType;
    private final BoundUnaryOperation boundOpType;
    private final TypeSymbol operandType;
    private final TypeSymbol returnType;

    public BoundUnaryOperator(OpType opType, BoundUnaryOperation boundOpType, TypeSymbol operandType, TypeSymbol returnType) {
        this.opType = opType;
        this.boundOpType = boundOpType;
        this.operandType = operandType;
        this.returnType = returnType;
    }

    public static BoundUnaryOperator error(OpType type, TypeSymbol operandType) {
        return new BoundUnaryOperator(type, BoundUnaryOperation.ERROR, operandType, TypeSymbol.ERROR);
    }

    public static BoundUnaryOperator bind(OpType opType, TypeSymbol operandType) {
        for (BoundUnaryOperator operator : operators) {
            if (operator.getOpType().equals(opType) && operator.getOperandType().isAssignableFrom(operandType)) {
                return operator;
            }
        }
        throw new InvalidOperationException();
    }

    private static BoundUnaryOperator[] operators = {
            new BoundUnaryOperator(OpType.NOT, BoundUnaryOperation.NOT, TypeSymbol.BOOL, TypeSymbol.BOOL),
            new BoundUnaryOperator(OpType.SUB, BoundUnaryOperation.NEGATION, TypeSymbol.INT, TypeSymbol.INT),
            new BoundUnaryOperator(OpType.SUB, BoundUnaryOperation.NEGATION, TypeSymbol.REAL, TypeSymbol.REAL),
    };

    public OpType getOpType() {
        return opType;
    }

    public BoundUnaryOperation getBoundOpType() {
        return boundOpType;
    }

    public TypeSymbol getOperandType() {
        return operandType;
    }

    public TypeSymbol getReturnType() {
        return returnType;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.UNARY_OPERATOR;
    }

    @Override
    public TypeSymbol getType() {
        return returnType;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.emptyIterator();
    }

    public enum BoundUnaryOperation {
        NOT,
        NEGATION,
        ERROR,
    }
}
