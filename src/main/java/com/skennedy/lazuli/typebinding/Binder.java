package com.skennedy.lazuli.typebinding;

import com.skennedy.lazuli.diagnostics.Error;
import com.skennedy.lazuli.exceptions.FunctionAlreadyDeclaredException;
import com.skennedy.lazuli.exceptions.ReadOnlyVariableException;
import com.skennedy.lazuli.exceptions.TypeMismatchException;
import com.skennedy.lazuli.exceptions.UndefinedFunctionException;
import com.skennedy.lazuli.exceptions.UndefinedVariableException;
import com.skennedy.lazuli.exceptions.VariableAlreadyDeclaredException;
import com.skennedy.lazuli.lexing.model.TokenType;
import com.skennedy.lazuli.lowering.BoundArrayLengthExpression;
import com.skennedy.lazuli.parsing.*;
import com.skennedy.lazuli.parsing.model.ExpressionType;
import com.skennedy.lazuli.parsing.model.IdentifierExpression;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Binder {

    private List<Error> errors;
    private BoundScope currentScope;

    public Binder() {
        currentScope = new BoundScope(null);
    }

    public BoundProgram bind(Program program) {

        errors = new ArrayList<>();

        List<BoundExpression> boundExpressions = new ArrayList<>();

        List<Expression> expressions = program.getExpressions();
        for (Expression expression : expressions) {
            boundExpressions.add(bind(expression));
        }
        return new BoundProgram(boundExpressions, errors);
    }

    public BoundExpression bind(Expression expression) {
        switch (expression.getExpressionType()) {

            case ARRAY_LITERAL_EXPR:
                return bindArrayLiteralExpression((ArrayLiteralExpression) expression);
            case ARRAY_ACCESS_EXPR:
                return bindArrayAccessExpression((ArrayAccessExpression) expression);
            case ASSIGNMENT_EXPR:
                return bindAssignmentExpression((AssignmentExpression) expression);
            case BINARY_EXPR:
                return bindBinaryExpression((BinaryExpression) expression);
            case BLOCK_EXPR:
                return bindBlockExpression((BlockExpression) expression);
            case FOR_EXPR:
                return bindForExpression((ForExpression) expression);
            case FOR_IN_EXPR:
                return bindForInExpression((ForInExpression) expression);
            case IF_EXPR:
                return bindIfExpression((IfExpression) expression);
            case LITERAL_EXPR:
                throw new IllegalStateException("Unhandled expression type: " + expression.getExpressionType());
            case PARENTHESISED_EXPR:
                return bind(((ParenthesisedExpression) expression).getExpression());
            case PRINT_EXPR:
                return bindPrintIntrinsic((PrintExpression) expression);
            case TYPEOF_EXPR:
                return bindTypeofIntrinsic((TypeofExpression) expression);
            case IDENTIFIER_EXPR:
                return bindIdentifierExpression((IdentifierExpression) expression);
            case INCREMENT_EXPR:
                return bindIncrementExpression((IncrementExpression) expression);
            case UNARY_EXPR:
                throw new IllegalStateException("Unhandled expression type: " + expression.getExpressionType());
            case VAR_DECLARATION_EXPR:
                return bindVariableDeclaration((VariableDeclarationExpression) expression);
            case WHILE_EXPR:
                return bindWhileExpression((WhileExpression) expression);
            case FUNC_DECLARATION_EXPR:
                return bindFunctionDeclarationExpression(((FunctionDeclarationExpression) expression));
            case FUNC_CALL_EXPR:
                return bindFunctionCallExpression((FunctionCallExpression) expression);
            case RETURN_EXPR:
                return bindReturnExpression((ReturnExpression) expression);
            case MATCH_EXPRESSION:
                return bindMatchExpression((MatchExpression) expression);
            case ARRAY_LEN_EXPR:
                return bindArrayLengthExpression((ArrayLengthExpression) expression);
            case ARRAY_ASSIGNMENT_EXPR:
                return bindArrayAssignmentExpression((ArrayAssignmentExpression) expression);
            case TUPLE_LITERAL_EXPR:
                return bindTupleLiteralExpression((TupleLiteralExpression) expression);
            case LAMBDA_EXPRESSION:
                return bindLambdaExpression((LambdaExpression) expression);
            case MAP_EXPRESSION:
                return bindMapExpression((MapExpression) expression);
            case ARRAY_DECLARATION_EXPR:
                return bindArrayDeclarationExpression((ArrayDeclarationExpression) expression);
            case YIELD_EXPRESSION:
                return bindYieldExpression((YieldExpression) expression);
            default:
                throw new IllegalStateException("Unexpected value: " + expression.getExpressionType());
        }
    }

    private BoundExpression bindYieldExpression(YieldExpression yieldExpression) {
        BoundExpression expression = bind(yieldExpression.getExpression());

        return new BoundYieldExpression(expression);
    }

    private BoundExpression bindArrayDeclarationExpression(ArrayDeclarationExpression arrayDeclarationExpression) {
        BoundExpression elementCount = bind(arrayDeclarationExpression.getElementCount());
        if (!elementCount.getType().isAssignableFrom(TypeSymbol.INT)) {
            throw new IllegalStateException("Element count must be of type int");
        }
        ArrayTypeSymbol typeSymbol = new ArrayTypeSymbol(parseType(arrayDeclarationExpression.getTypeExpression()));

        return new BoundArrayDeclarationExpression(typeSymbol, elementCount);
    }

    private BoundExpression bindMapExpression(MapExpression mapExpression) {
        BoundExpression boundMapperFunction = bind(mapExpression.getMapFunction());
        if (!boundMapperFunction.getType().isAssignableFrom(TypeSymbol.FUNCTION)) {
            throw new IllegalStateException("Map must take a function");
        }
        BoundExpression boundOperand = bind(mapExpression.getExpression());

        if (!(boundOperand.getType() instanceof ArrayTypeSymbol)) {
            throw new IllegalStateException("Map can only operate on array types");
        }
        return new BoundMapExpression(boundMapperFunction, boundOperand);
    }

    private BoundExpression bindTupleLiteralExpression(TupleLiteralExpression tupleLiteralExpression) {

        List<BoundExpression> boundElements = new ArrayList<>();
        for (Expression element : tupleLiteralExpression.getElements()) {
            boundElements.add(bind(element));
        }

        return new BoundTupleLiteralExpression(boundElements);
    }

    private BoundExpression bindMatchExpression(MatchExpression matchExpression) {

        BoundExpression operand = bind(matchExpression.getOperand());

        List<BoundMatchCaseExpression> boundMatchCaseExpressions = new ArrayList<>();

        TypeSymbol type = null;
        for (MatchCaseExpression caseExpression : matchExpression.getCaseExpressions()) {
            BoundMatchCaseExpression boundMatchCaseExpression = bindCaseExpression(caseExpression);
            if (type != null && !type.isAssignableFrom(boundMatchCaseExpression.getType())) {
                throw new TypeMismatchException(type, boundMatchCaseExpression.getType());
            }
            type = boundMatchCaseExpression.getType();
            if (boundMatchCaseExpression.getCaseExpression() != null && boundMatchCaseExpression.getCaseExpression().getType() != operand.getType()) {
                throw new TypeMismatchException(operand.getType(), boundMatchCaseExpression.getCaseExpression().getType());
            }
            boundMatchCaseExpressions.add(boundMatchCaseExpression);
        }

        return new BoundMatchExpression(type, operand, boundMatchCaseExpressions);
    }

    private BoundMatchCaseExpression bindCaseExpression(MatchCaseExpression matchCaseExpression) {

        BoundExpression caseExpression;
        if (matchCaseExpression.getCaseExpression().getExpressionType() == ExpressionType.IDENTIFIER_EXPR
                && ((IdentifierExpression) matchCaseExpression.getCaseExpression()).getTokenType() == TokenType.ELSE_KEYWORD) {
            caseExpression = null; //TODO: Is this the best way to denote a default case?
        } else {
            caseExpression = bind(matchCaseExpression.getCaseExpression());
        }
        BoundExpression boundThenExpression = bind(matchCaseExpression.getThenExpression());

        return new BoundMatchCaseExpression(caseExpression, boundThenExpression);
    }

    private BoundExpression bindReturnExpression(ReturnExpression returnExpression) {
        BoundExpression returnValue = bind(returnExpression.getReturnValue());

        return new BoundReturnExpression(returnValue);
    }

    private BoundExpression bindArrayLiteralExpression(ArrayLiteralExpression arrayLiteralExpression) {

        List<BoundExpression> boundElements = new ArrayList<>();
        for (Expression element : arrayLiteralExpression.getElements()) {
            boundElements.add(bind(element));
        }

        return new BoundArrayLiteralExpression(boundElements);
    }

    private BoundArrayAccessExpression bindArrayAccessExpression(ArrayAccessExpression arrayAccessExpression) {
        IdentifierExpression identifier = arrayAccessExpression.getIdentifier();
        Optional<VariableSymbol> variable = currentScope.tryLookupVariable((String) identifier.getValue());
        if (variable.isEmpty()) {
            throw new UndefinedVariableException((String) identifier.getValue());
        }
//        if (!variable.get().getType().isAssignableFrom(TypeSymbol.INT_ARRAY)) {
//            errors.add(Error.raiseTypeMismatch(TypeSymbol.INT_ARRAY, variable.get().getType()));
//        }
        BoundExpression index = bind(arrayAccessExpression.getIndex());

        if (!index.getType().isAssignableFrom(TypeSymbol.INT)) {
            errors.add(Error.raiseTypeMismatch(TypeSymbol.INT, index.getType()));
        }
        return new BoundArrayAccessExpression(new BoundVariableExpression(variable.get()), index);
    }

    private BoundExpression bindArrayLengthExpression(ArrayLengthExpression arrayLengthExpression) {

        BoundExpression boundExpression = bind(arrayLengthExpression.getExpression());
//        if (!boundExpression.getType().isAssignableFrom(TypeSymbol.INT_ARRAY) && boundExpression.getType() != TypeSymbol.TUPLE) {
//            errors.add(Error.raiseTypeMismatch(TypeSymbol.INT_ARRAY, boundExpression.getType()));
//        }

        return new BoundArrayLengthExpression(boundExpression);
    }

    private BoundExpression bindArrayAssignmentExpression(ArrayAssignmentExpression expression) {

        BoundArrayAccessExpression boundArrayAccessExpression = bindArrayAccessExpression(expression.getArrayAccessExpression());
        BoundExpression assignment = bind(expression.getAssignment());

        return new BoundArrayAssignmentExpression(boundArrayAccessExpression, assignment);
    }

    private BoundExpression bindForExpression(ForExpression forExpression) {

        currentScope = new BoundScope(currentScope);
        BoundRangeExpression range = bindRangeExpression(forExpression.getRangeExpression());

        TypeSymbol type = parseType(forExpression.getTypeExpression());

        if (!type.isAssignableFrom(range.getType())) {
            errors.add(Error.raiseTypeMismatch(type, range.getType()));
        }

        VariableSymbol variable = getVariableSymbol(type, forExpression.getIdentifier(), null, true);

        try {
            currentScope.declareVariable((String) forExpression.getIdentifier().getValue(), variable);
        } catch (VariableAlreadyDeclaredException vade) {
            errors.add(Error.raiseVariableAlreadyDeclared((String) forExpression.getIdentifier().getValue()));
        }
        BoundExpression guard = null;
        if (forExpression.getGuard() != null) {
            guard = bind(forExpression.getGuard());
        }
        BoundExpression body = bind(forExpression.getBody());

        currentScope = currentScope.getParentScope();

        return new BoundForExpression(variable, range, guard, body);
    }

    private BoundRangeExpression bindRangeExpression(RangeExpression rangeExpression) {

        BoundExpression lowerBound = bind(rangeExpression.getLowerBound());
        BoundExpression upperBound = bind(rangeExpression.getUpperBound());

        BoundExpression step = null;
        if (rangeExpression.getStep() != null) {
            step = bind(rangeExpression.getStep());
        }

        return new BoundRangeExpression(lowerBound, upperBound, step);
    }

    private BoundExpression bindForInExpression(ForInExpression forInExpression) {
        currentScope = new BoundScope(currentScope);

        BoundExpression iterable = bind(forInExpression.getIterable());
//        if (!iterable.getType().isAssignableFrom(TypeSymbol.INT_ARRAY) && iterable.getType() != TypeSymbol.TUPLE) {
//            throw new IllegalStateException("For-in expression only applicable to Array or Tuple types");
//        }

        TypeSymbol type = parseType(forInExpression.getTypeExpression());

        if (!type.isAssignableFrom(iterable.getType())) {
            errors.add(Error.raiseTypeMismatch(type, iterable.getType()));
        }
        VariableSymbol variable = getVariableSymbol(type, forInExpression.getIdentifier(), null, false);

        try {
            currentScope.declareVariable((String) forInExpression.getIdentifier().getValue(), variable);
        } catch (VariableAlreadyDeclaredException vade) {
            errors.add(Error.raiseVariableAlreadyDeclared((String) forInExpression.getIdentifier().getValue()));
        }

        BoundExpression guard = null;
        if (forInExpression.getGuard() != null) {
            guard = bind(forInExpression.getGuard());
        }

        BoundExpression body = bind(forInExpression.getBody());

        currentScope = currentScope.getParentScope();

        return new BoundForInExpression(variable, iterable, guard, body);
    }

    private TypeSymbol parseType(TypeExpression typeExpression) {
        TypeSymbol typeSymbol;

        switch (typeExpression.getIdentifier().getTokenType()) {
            case VOID_KEYWORD:
                typeSymbol = TypeSymbol.VOID;
                break;
            case INT_KEYWORD:
                typeSymbol = TypeSymbol.INT;
                break;
            case BOOL_KEYWORD:
                typeSymbol = TypeSymbol.BOOL;
                break;
            case STRING_KEYWORD:
                typeSymbol = TypeSymbol.STRING;
                break;
            case REAL_KEYWORD:
                typeSymbol = TypeSymbol.REAL;
                break;
            case FUNCTION_KEYWORD:
                typeSymbol = TypeSymbol.FUNCTION;
                break;
            case TUPLE_KEYWORD:
                typeSymbol = TypeSymbol.TUPLE;
                break;
            case VAR_KEYWORD:
                typeSymbol = TypeSymbol.VAR;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + typeExpression.getIdentifier().getTokenType());
        }
        //TODO: This doesn't do N-Dimensional arrays yet
        if (typeExpression.getOpenSquareBracket() != null && typeExpression.getCloseSquareBracket() != null) {
            return new ArrayTypeSymbol(typeSymbol);
        }
        return typeSymbol;
    }

    private VariableSymbol getVariableSymbol(TypeSymbol type, IdentifierExpression identifier, BoundExpression guard, boolean readOnly) {
        return new VariableSymbol((String) identifier.getValue(), type, guard, readOnly);
    }

    private BoundExpression bindIfExpression(IfExpression ifExpression) {

        BoundExpression condition = bind(ifExpression.getCondition());
        if (!condition.getType().isAssignableFrom(TypeSymbol.BOOL)) {
            errors.add(Error.raiseTypeMismatch(TypeSymbol.BOOL, condition.getType()));
            throw new TypeMismatchException(TypeSymbol.BOOL, condition.getType());
        }
        BoundExpression body = bind(ifExpression.getBody());

        BoundExpression elseBody = null;
        if (ifExpression.getElseBody() != null) {
            elseBody = bind(ifExpression.getElseBody());
        }
        return new BoundIfExpression(condition, body, elseBody);
    }

    private BoundExpression bindBinaryExpression(BinaryExpression binaryExpression) {

        BoundExpression left = bind(binaryExpression.getLeft());
        BoundExpression right = bind(binaryExpression.getRight());
        BoundBinaryOperator operator = BoundBinaryOperator.bind(binaryExpression.getOperation(), left.getType(), right.getType());

        return new BoundBinaryExpression(left, operator, right);
    }

    private BoundExpression bindIdentifierExpression(IdentifierExpression identifierExpression) {

        if (identifierExpression.getTokenType() == TokenType.INT_LITERAL || identifierExpression.getTokenType() == TokenType.STRING_LITERAL) {
            return new BoundLiteralExpression(identifierExpression.getValue());

        } else if (identifierExpression.getTokenType() == TokenType.TRUE_KEYWORD) {
            return new BoundLiteralExpression(true);

        } else if (identifierExpression.getTokenType() == TokenType.FALSE_KEYWORD) {
            return new BoundLiteralExpression(false);
        }

        Optional<VariableSymbol> variable = currentScope.tryLookupVariable((String) identifierExpression.getValue());
        if (variable.isPresent()) {
            return new BoundVariableExpression(variable.get());
        }
        throw new UndefinedVariableException((String) identifierExpression.getValue());
    }

    private BoundExpression bindIncrementExpression(IncrementExpression incrementExpression) {

        Optional<VariableSymbol> variable = currentScope.tryLookupVariable((String) incrementExpression.getIdentifier().getValue());
        if (variable.isEmpty()) {
            throw new UndefinedVariableException((String) incrementExpression.getIdentifier().getValue());
        }

        int increment;
        if (incrementExpression.getOperator().getTokenType() == TokenType.INCREMENT) {
            increment = 1;
        } else if (incrementExpression.getOperator().getTokenType() == TokenType.DECREMENT) {
            increment = -1;
        } else {
            throw new IllegalStateException("Unknown operator in increment expression: " + incrementExpression.getOperator().getTokenType());
        }

        return new BoundIncrementExpression(variable.get(), new BoundLiteralExpression(increment));
    }

    private BoundExpression bindPrintIntrinsic(PrintExpression printExpression) {
        BoundExpression boundExpression = bind(printExpression.getExpression());

        return new BoundPrintExpression(boundExpression);
    }

    private BoundExpression bindTypeofIntrinsic(TypeofExpression typeofExpression) {
        BoundExpression boundExpression = bind(typeofExpression.getExpression());

        return new BoundTypeofExpression(boundExpression);
    }

    private BoundExpression bindAssignmentExpression(AssignmentExpression assignmentExpression) {

        BoundExpression initialiser = bind(assignmentExpression.getAssignment());

        IdentifierExpression identifier = assignmentExpression.getIdentifier();
        Optional<VariableSymbol> scopedVariable = currentScope.tryLookupVariable((String) identifier.getValue());
        if (!scopedVariable.isPresent()) {
            throw new UndefinedVariableException((String) identifier.getValue());
        }
        VariableSymbol variable = scopedVariable.get();
        if (variable.isReadOnly()) {
            throw new ReadOnlyVariableException(variable.getName());
        }

        if (!variable.getType().isAssignableFrom(initialiser.getType())) {
            throw new TypeMismatchException(variable.getType(), initialiser.getType());
        }

        return new BoundAssignmentExpression(variable, variable.getGuard(), initialiser);
    }

    private BoundExpression bindFunctionDeclarationExpression(FunctionDeclarationExpression functionDeclarationExpression) {

        IdentifierExpression identifier = functionDeclarationExpression.getIdentifier();

        TypeSymbol type = parseType(functionDeclarationExpression.getTypeIdentifier());

        currentScope = new BoundScope(currentScope);

        //Declare the arguments within the function's scope
        List<BoundFunctionArgumentExpression> arguments = new ArrayList<>();
        for (FunctionArgumentExpression argumentExpression : functionDeclarationExpression.getArguments()) {
            arguments.add(bindFunctionArgumentExpression(argumentExpression));
        }

        FunctionSymbol functionSymbol = new FunctionSymbol((String) identifier.getValue(), type, arguments, null);
        try {
            //Declare the function in the parent scope
            currentScope.getParentScope().declareFunction((String) identifier.getValue(), functionSymbol);
        } catch (FunctionAlreadyDeclaredException fade) {
            errors.add(Error.raiseVariableAlreadyDeclared((String) identifier.getValue()));
        }

        BoundBlockExpression body = bindBlockExpression(functionDeclarationExpression.getBody());

        if (functionSymbol.getType() != TypeSymbol.VOID) {
            analyzeBody(functionSymbol, body.getExpressions().iterator());
        }

        currentScope = currentScope.getParentScope();

        return new BoundFunctionDeclarationExpression(functionSymbol, arguments, body);
    }

    private BoundLambdaExpression bindLambdaExpression(LambdaExpression lambdaExpression) {

        //IdentifierExpression identifier = functionDeclarationExpression.getIdentifier();

        currentScope = new BoundScope(currentScope);

        //Declare the arguments within the function's scope
        List<BoundFunctionArgumentExpression> boundArguments = new ArrayList<>();
        for (FunctionArgumentExpression argumentExpression : lambdaExpression.getArgumentExpressions()) {
            boundArguments.add(bindFunctionArgumentExpression(argumentExpression));
        }

        BoundExpression boundBody = new BoundBlockExpression(
                new BoundReturnExpression(bind(lambdaExpression.getExpression()))
        );
        TypeSymbol type = boundBody.getType();

        String anonymousFunctionIdentifier = "lambda-function-" + UUID.randomUUID().toString();

        FunctionSymbol functionSymbol = new FunctionSymbol(anonymousFunctionIdentifier, type, boundArguments, null);
        try {
            //Declare the function in the parent scope
            currentScope.getParentScope().declareFunction(anonymousFunctionIdentifier, functionSymbol);
        } catch (FunctionAlreadyDeclaredException fade) {
            errors.add(Error.raiseVariableAlreadyDeclared(anonymousFunctionIdentifier));
        }

//        if (functionSymbol.getType() != TypeSymbol.VOID) {
//            analyzeBody(functionSymbol, body.getExpressions().iterator());
//        }

        currentScope = currentScope.getParentScope();

        return new BoundLambdaExpression(boundArguments, boundBody);
    }

    private void analyzeBody(FunctionSymbol function, Iterator<BoundExpression> expressions) {

        //BFS the body to ensure all paths return a value of the right type
        for (Iterator<BoundExpression> it = expressions; it.hasNext(); ) {
            BoundExpression expression = it.next();
            if (expression == null) {
                return;
            }
            if (expression.getBoundExpressionType() == BoundExpressionType.RETURN) {
                if (!expression.getType().isAssignableFrom(function.getType())) {
                    throw new TypeMismatchException(function.getType(), expression.getType());
                }
                return;
            }
        }
    }

    private BoundFunctionArgumentExpression bindFunctionArgumentExpression(FunctionArgumentExpression argumentExpression) {

        IdentifierExpression identifier = argumentExpression.getIdentifier();
        TypeSymbol type = parseType(argumentExpression.getTypeExpression());

        //Create placeholder
        try {
            currentScope.declareVariable((String) identifier.getValue(), new VariableSymbol((String) identifier.getValue(), type, null, false));
        } catch (VariableAlreadyDeclaredException vade) {
            errors.add(Error.raiseVariableAlreadyDeclared((String) identifier.getValue()));
        }

        BoundExpression guard = null;
        if (argumentExpression.getGuard() != null) {
            guard = bind(argumentExpression.getGuard());
        }

        VariableSymbol argument = getVariableSymbol(type, identifier, guard, argumentExpression.getConstKeyword() != null);

        currentScope.reassignVariable((String) identifier.getValue(), argument);

        return new BoundFunctionArgumentExpression(argument, guard);
    }

    private BoundExpression bindFunctionCallExpression(FunctionCallExpression functionCallExpression) {

        IdentifierExpression identifier = functionCallExpression.getIdentifier();
        Optional<FunctionSymbol> scopedFunction = currentScope.tryLookupFunction((String) identifier.getValue());
        if (scopedFunction.isEmpty()) {
            throw new UndefinedFunctionException((String) identifier.getValue());
        }
        FunctionSymbol function = scopedFunction.get();

        List<BoundExpression> boundArguments = new ArrayList<>();
        List<BoundFunctionArgumentExpression> arguments = function.getArguments();
        List<Expression> functionCallExpressionArguments = functionCallExpression.getArguments();
        for (int i = 0; i < functionCallExpressionArguments.size(); i++) {
            BoundExpression boundArgument = bind(functionCallExpressionArguments.get(i));
            if (!arguments.get(i).getType().isAssignableFrom(boundArgument.getType())) {
                throw new TypeMismatchException(arguments.get(i).getType(), boundArgument.getType());
            }
            boundArguments.add(boundArgument);
        }

        return new BoundFunctionCallExpression(function, boundArguments);
    }

    private BoundExpression bindVariableDeclaration(VariableDeclarationExpression variableDeclarationExpression) {

        BoundExpression initialiser = null;
        if (variableDeclarationExpression.getInitialiser() != null) {
            initialiser = bind(variableDeclarationExpression.getInitialiser());
        }

        IdentifierExpression identifier = variableDeclarationExpression.getIdentifier();

        //Create placeholder
        try {
            TypeSymbol type = parseType(variableDeclarationExpression.getTypeExpression());
            currentScope.declareVariable((String) identifier.getValue(), new VariableSymbol((String) identifier.getValue(), type, null, false));
        } catch (VariableAlreadyDeclaredException vade) {
            errors.add(Error.raiseVariableAlreadyDeclared((String) identifier.getValue()));
        }

        BoundExpression guard = null;
        if (variableDeclarationExpression.getGuard() != null) {
            guard = bind(variableDeclarationExpression.getGuard());
            assert guard.getType().isAssignableFrom(TypeSymbol.BOOL);
        }

        TypeSymbol type = parseType(variableDeclarationExpression.getTypeExpression());

        if (initialiser != null && !type.isAssignableFrom(initialiser.getType())) {
            errors.add(Error.raiseTypeMismatch(type, initialiser.getType()));
        }

        if (type == TypeSymbol.FUNCTION) {
            //We're defining a lambda
            if (!(variableDeclarationExpression.getInitialiser() instanceof LambdaExpression)) {
                throw new IllegalStateException("Functions can not be initialised by " + variableDeclarationExpression.getInitialiser().getClass());
            }
            BoundLambdaExpression lambdaExpression = bindLambdaExpression((LambdaExpression) variableDeclarationExpression.getInitialiser());

            //TODO: guarded lambdas
            FunctionSymbol function = new FunctionSymbol((String) variableDeclarationExpression.getIdentifier().getValue(), lambdaExpression.getBody().getType(), lambdaExpression.getArguments(), null);
            currentScope.declareFunction(function.getName(), function);
            VariableSymbol variable = getVariableSymbol(type, identifier, guard, variableDeclarationExpression.getConstKeyword() != null);
            return new BoundFunctionDeclarationExpression(function, lambdaExpression.getArguments(), new BoundBlockExpression(
                    new BoundReturnExpression(lambdaExpression.getBody())
            ));
        }

        VariableSymbol variable = getVariableSymbol(type, identifier, guard, variableDeclarationExpression.getConstKeyword() != null);

        currentScope.reassignVariable((String) identifier.getValue(), variable);
        return new BoundVariableDeclarationExpression(variable, guard, initialiser, variableDeclarationExpression.getConstKeyword() != null);
    }

    private BoundExpression bindWhileExpression(WhileExpression whileExpression) {

        BoundExpression condition = bind(whileExpression.getCondition());
        if (!condition.getType().isAssignableFrom(TypeSymbol.BOOL)) {
            throw new TypeMismatchException(TypeSymbol.BOOL, condition.getType());
        }

        BoundExpression body = bind(whileExpression.getBody());

        return new BoundWhileExpression(condition, body);
    }

    private BoundBlockExpression bindBlockExpression(BlockExpression blockExpression) {
        List<BoundExpression> boundExpressions = new ArrayList<>();

        currentScope = new BoundScope(currentScope);

        for (Expression expression : blockExpression.getExpressions()) {
            boundExpressions.add(bind(expression));
        }

        currentScope = currentScope.getParentScope();

        return new BoundBlockExpression(boundExpressions);
    }
}
