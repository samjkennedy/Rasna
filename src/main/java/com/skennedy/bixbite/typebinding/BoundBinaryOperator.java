package com.skennedy.bixbite.typebinding;

import com.skennedy.bixbite.exceptions.InvalidOperationException;
import com.skennedy.bixbite.parsing.model.OpType;

import java.util.Collections;
import java.util.Iterator;

public class BoundBinaryOperator implements BoundExpression {

    private final OpType opType;
    private final BoundBinaryOperation boundOpType;
    private final TypeSymbol leftType;
    private final TypeSymbol rightType;
    private final TypeSymbol returnType;

    private BoundBinaryOperator(OpType opType, BoundBinaryOperation boundOpType, TypeSymbol leftType, TypeSymbol rightType, TypeSymbol returnType) {
        this.opType = opType;
        this.boundOpType = boundOpType;
        this.leftType = leftType;
        this.rightType = rightType;
        this.returnType = returnType;
    }

    public static BoundBinaryOperator bind(OpType opType, TypeSymbol leftType, TypeSymbol rightType) {
        for (BoundBinaryOperator operator : operators) {
            if (operator.getOpType().equals(opType) && operator.getLeftType().equals(leftType) && operator.getRightType().equals(rightType)) {
                return operator;
            }
        }
        throw new InvalidOperationException(); //TODO: Better error handling
    }

    public OpType getOpType() {
        return opType;
    }

    public BoundBinaryOperation getBoundOpType() {
        return boundOpType;
    }

    public TypeSymbol getLeftType() {
        return leftType;
    }

    public TypeSymbol getRightType() {
        return rightType;
    }

    public TypeSymbol getReturnType() {
        return returnType;
    }

    private static BoundBinaryOperator[] operators = {
            new BoundBinaryOperator(OpType.ADD, BoundBinaryOperation.ADDITION, TypeSymbol.INT, TypeSymbol.INT, TypeSymbol.INT),
            new BoundBinaryOperator(OpType.SUB, BoundBinaryOperation.SUBTRACTION, TypeSymbol.INT, TypeSymbol.INT, TypeSymbol.INT),
            new BoundBinaryOperator(OpType.MUL, BoundBinaryOperation.MULTIPLICATION, TypeSymbol.INT, TypeSymbol.INT, TypeSymbol.INT),
            new BoundBinaryOperator(OpType.DIV, BoundBinaryOperation.DIVISION, TypeSymbol.INT, TypeSymbol.INT, TypeSymbol.INT),
            new BoundBinaryOperator(OpType.MOD, BoundBinaryOperation.REMAINDER, TypeSymbol.INT, TypeSymbol.INT, TypeSymbol.INT),
            new BoundBinaryOperator(OpType.GT, BoundBinaryOperation.GREATER_THAN, TypeSymbol.INT, TypeSymbol.INT, TypeSymbol.BOOL),
            new BoundBinaryOperator(OpType.LT, BoundBinaryOperation.LESS_THAN, TypeSymbol.INT, TypeSymbol.INT, TypeSymbol.BOOL),
            new BoundBinaryOperator(OpType.GTEQ, BoundBinaryOperation.GREATER_THAN_OR_EQUAL, TypeSymbol.INT, TypeSymbol.INT, TypeSymbol.BOOL),
            new BoundBinaryOperator(OpType.LTEQ, BoundBinaryOperation.LESS_THAN_OR_EQUAL, TypeSymbol.INT, TypeSymbol.INT, TypeSymbol.BOOL),
            new BoundBinaryOperator(OpType.EQ, BoundBinaryOperation.EQUALS, TypeSymbol.INT, TypeSymbol.INT, TypeSymbol.BOOL),
            new BoundBinaryOperator(OpType.EQ, BoundBinaryOperation.EQUALS, TypeSymbol.BOOL, TypeSymbol.BOOL, TypeSymbol.BOOL),
            new BoundBinaryOperator(OpType.NEQ, BoundBinaryOperation.NOT_EQUALS, TypeSymbol.INT, TypeSymbol.INT, TypeSymbol.BOOL),
            new BoundBinaryOperator(OpType.NEQ, BoundBinaryOperation.NOT_EQUALS, TypeSymbol.BOOL, TypeSymbol.BOOL, TypeSymbol.BOOL),
            new BoundBinaryOperator(OpType.LAND, BoundBinaryOperation.BOOLEAN_AND, TypeSymbol.BOOL, TypeSymbol.BOOL, TypeSymbol.BOOL),
            new BoundBinaryOperator(OpType.LOR, BoundBinaryOperation.BOOLEAN_OR, TypeSymbol.BOOL, TypeSymbol.BOOL, TypeSymbol.BOOL),
    };

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.BINARY_OPERATOR;
    }

    @Override
    public TypeSymbol getType() {
        return returnType;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return Collections.emptyIterator();
    }

    public enum BoundBinaryOperation {
        ADDITION,
        SUBTRACTION,
        MULTIPLICATION,
        DIVISION,
        REMAINDER,
        GREATER_THAN,
        LESS_THAN,
        GREATER_THAN_OR_EQUAL,
        LESS_THAN_OR_EQUAL,
        EQUALS,
        NOT_EQUALS,
        BOOLEAN_OR,
        BOOLEAN_AND
    }
}
