package com.skennedy.rasna.typebinding;

import org.apache.commons.collections4.IteratorUtils;

import java.util.Iterator;
import java.util.List;

public class BoundFunctionSignatureExpression implements BoundExpression {

    private final String identifier;
    private final List<BoundFunctionParameterExpression> functionParameterExpressions;
    private final TypeSymbol returnType;

    public BoundFunctionSignatureExpression(String identifier, List<BoundFunctionParameterExpression> functionParameterExpressions, TypeSymbol returnType) {

        this.identifier = identifier;
        this.functionParameterExpressions = functionParameterExpressions;
        this.returnType = returnType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<BoundFunctionParameterExpression> getFunctionParameterExpressions() {
        return functionParameterExpressions;
    }

    public TypeSymbol getReturnType() {
        return returnType;
    }

    @Override
    public BoundExpressionType getBoundExpressionType() {
        return BoundExpressionType.FUNCTION_SIGNATURE;
    }

    @Override
    public TypeSymbol getType() {
        return returnType;
    }

    @Override
    public Iterator<BoundExpression> getChildren() {
        return IteratorUtils.emptyIterator();
    }
}
