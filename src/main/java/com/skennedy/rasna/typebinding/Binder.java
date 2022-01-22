package com.skennedy.rasna.typebinding;

import com.skennedy.rasna.diagnostics.BindingError;
import com.skennedy.rasna.exceptions.FunctionAlreadyDeclaredException;
import com.skennedy.rasna.exceptions.InvalidOperationException;
import com.skennedy.rasna.exceptions.TypeAlreadyDeclaredException;
import com.skennedy.rasna.exceptions.TypeMismatchException;
import com.skennedy.rasna.exceptions.UndefinedVariableException;
import com.skennedy.rasna.exceptions.VariableAlreadyDeclaredException;
import com.skennedy.rasna.lexing.model.Location;
import com.skennedy.rasna.lexing.model.Token;
import com.skennedy.rasna.lexing.model.TokenType;
import com.skennedy.rasna.lowering.BoundArrayLengthExpression;
import com.skennedy.rasna.parsing.*;
import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.skennedy.rasna.typebinding.TypeSymbol.INT;
import static com.skennedy.rasna.typebinding.TypeSymbol.UNIT;

public class Binder {

    private List<BindingError> errors;
    private List<BindingWarning> warnings;
    private BoundScope currentScope;

    public Binder() {
        currentScope = new BoundScope(null);
    }

    public BoundProgram bind(Program program) {

        errors = new ArrayList<>();
        warnings = new ArrayList<>();

        List<BoundExpression> boundExpressions = new ArrayList<>();

        List<Expression> expressions = program.getExpressions();
        for (Expression expression : expressions) {
            boundExpressions.add(bind(expression));
        }
        return new BoundProgram(boundExpressions, errors, warnings);
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
                return bindUnaryExpression((UnaryExpression) expression);
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
            case MEMBER_ASSIGNMENT_EXPR:
                return bindMemberAssignmentExpression((MemberAssignmentExpression) expression);
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
            case STRUCT_DECLARATION_EXPR:
                return bindStructDeclarationExpression((StructDeclarationExpression) expression);
            case MEMBER_ACCESSOR_EXPR:
                return bindMemberAccessorExpression((MemberAccessorExpression) expression);
            case NAMESPACE:
                return bindNamespace((NamespaceExpression) expression);
            case NAMESPACE_ACCESSOR_EXPR:
                return bindNamespaceAccessorExpression((NamespaceAccessorExpression) expression);
            case CAST_EXPR:
                return bindCastExpression((CastExpression) expression);
            case STRUCT_LITERAL_EXPRESSION:
                return bindStructLiteralExpression((StructLiteralExpression) expression);
            case TYPE_TEST_EXPR:
                return bindTypeTestExpression((TypeTestExpression) expression);
            case TUPLE_INDEX_EXPR:
                return bindTupleIndexExpression((TupleIndexExpression) expression);
            case ENUM_DECLARATION_EXPR:
                return bindEnumDeclarationExpression((EnumDeclarationExpression) expression);
            default:
                throw new IllegalStateException("Unexpected value: " + expression.getExpressionType());
        }
    }

    private BoundExpression bindTupleIndexExpression(TupleIndexExpression tupleIndexExpression) {
        BoundExpression boundTuple = bind(tupleIndexExpression.getTuple());
        if (!(boundTuple.getType() instanceof TupleTypeSymbol)) {
            errors.add(BindingError.raise("Expected a tuple type but got `" + boundTuple.getType() + "` instead", tupleIndexExpression.getTuple().getSpan()));
            return new BoundErrorExpression();
        }
        if (((TupleTypeSymbol) boundTuple.getType()).getTypes().size() <= (int) tupleIndexExpression.getIndex().getValue()) {
            errors.add(BindingError.raiseOutOfBounds((int) tupleIndexExpression.getIndex().getValue(), (TupleTypeSymbol) boundTuple.getType(), tupleIndexExpression.getSpan()));
        }
        return new BoundTupleIndexExpression(boundTuple, new BoundLiteralExpression(tupleIndexExpression.getIndex().getValue()));
    }

    private BoundExpression bindTypeTestExpression(TypeTestExpression typeTestExpression) {
        BoundExpression expression = bind(typeTestExpression.getExpression());
        IdentifierExpression typeLiteral = typeTestExpression.getTypeLiteral();

        return new BoundTypeTestExpression(expression, getTypeSymbol(typeLiteral));
    }

    private BoundExpression bindStructLiteralExpression(StructLiteralExpression structLiteralExpression) {

        if (structLiteralExpression.getTypeExpression().getIdentifier() instanceof TupleTypeExpression) {
            throw new UnsupportedOperationException("TupleTypeSymbol types are not yet supported in struct literals");
        }
        IdentifierExpression typeExpression = (IdentifierExpression) structLiteralExpression.getTypeExpression().getIdentifier();

        Optional<TypeSymbol> type = currentScope.tryLookupType((String) typeExpression.getValue());
        if (type.isEmpty()) {
            //This should never happen
            errors.add(BindingError.raiseUnknownType((String) typeExpression.getValue(), structLiteralExpression.getSpan()));
            return new BoundErrorExpression();
        }

        List<BoundExpression> members = new ArrayList<>();
        List<Expression> structLiteralExpressionMembers = structLiteralExpression.getMembers();
        for (Expression member : structLiteralExpressionMembers) {
            members.add(bind(member));
        }

        List<VariableSymbol> values = new ArrayList<>(type.get().getFields().values());
        if (values.size() != structLiteralExpression.getMembers().size()) {
            errors.add(BindingError.raiseUnknownStruct((String) typeExpression.getValue(), members, structLiteralExpression.getSpan()));
            return new BoundErrorExpression();
        }

        for (int i = 0; i < structLiteralExpressionMembers.size(); i++) {
            BoundExpression boundMember = members.get(i);
            TypeSymbol expectedType = values.get(i).getType();
            if (!expectedType.isAssignableFrom(boundMember.getType())) {

                //TODO: Can be argType mismatch, e.g. expected type X in position Y of function Z
                errors.add(BindingError.raiseTypeMismatch(expectedType, boundMember.getType(), structLiteralExpression.getMembers().get(i).getSpan()));
            }
        }

        if (values.size() != members.size()) {
            //This could be a little misleading, need signature info in the functionsymbol
            errors.add(BindingError.raiseUnknownFunction((String) typeExpression.getValue(), members, structLiteralExpression.getSpan()));
            return new BoundErrorExpression();
        }

        return new BoundStructLiteralExpression(type.get(), members);
    }

    private BoundExpression bindCastExpression(CastExpression castExpression) {

        BoundExpression boundExpression = bind(castExpression.getExpression());
        TypeSymbol type = parseType(castExpression.getType());

        return new BoundCastExpression(boundExpression, type);
    }

    private BoundExpression bindNamespaceAccessorExpression(NamespaceAccessorExpression namespaceAccessorExpression) {
        IdentifierExpression namespace = namespaceAccessorExpression.getNamespace();

        Optional<BoundScope> namespaceScope = currentScope.tryLookupNamespace((String) namespace.getValue());
        if (namespaceScope.isEmpty()) {
            errors.add(BindingError.raiseUnknownNamespace((String) namespace.getValue(), namespaceAccessorExpression.getSpan()));
            return new BoundErrorExpression();
        }
        BoundScope previous = currentScope;
        currentScope = BoundScope.merge(previous, namespaceScope.get());

        BoundExpression boundExpression = bind(namespaceAccessorExpression.getExpression());

        currentScope = previous;

        return boundExpression;
    }

    private BoundExpression bindNamespace(NamespaceExpression namespaceExpression) {

        if (namespaceExpression.isInline()) {
            List<BoundExpression> boundExpressions = new ArrayList<>();

            for (Expression expression : namespaceExpression.getBody().getExpressions()) {
                boundExpressions.add(bind(expression));
            }
            currentScope.declareNamespace((String) namespaceExpression.getNamespace().getValue(), currentScope);

            return new BoundBlockExpression(boundExpressions);
        }

        currentScope = new BoundScope(currentScope);

        List<BoundExpression> boundExpressions = new ArrayList<>();

        for (Expression expression : namespaceExpression.getBody().getExpressions()) {
            boundExpressions.add(bind(expression));
        }
        BoundScope namespaceScope = currentScope;

        currentScope = currentScope.getParentScope();

        currentScope.declareNamespace((String) namespaceExpression.getNamespace().getValue(), namespaceScope);

        return new BoundBlockExpression(boundExpressions);
    }

    private BoundExpression bindMemberAccessorExpression(MemberAccessorExpression memberAccessorExpression) {
        BoundExpression boundOwner = bind(memberAccessorExpression.getOwner());
        if (boundOwner instanceof BoundErrorExpression) {
            return boundOwner;
        }

        TypeSymbol type;
        if (boundOwner instanceof BoundTypeExpression) {
            type = ((BoundTypeExpression) boundOwner).getTypeSymbol();
        } else if (TypeSymbol.getPrimitives().contains(boundOwner.getType()) || boundOwner.getType() instanceof TupleTypeSymbol) {
            type = boundOwner.getType();
        } else if (boundOwner.getType() instanceof ArrayTypeSymbol) {
            type = ((ArrayTypeSymbol) boundOwner.getType()).getType();
        } else if (boundOwner.getType() != null) {
            Optional<TypeSymbol> typeSymbol = currentScope.tryLookupType(boundOwner.getType().getName());
            if (typeSymbol.isEmpty()) {
                throw new IllegalStateException("No such type " + boundOwner.getType().getName() + " in scope, possibly a parser bug");
            }
            type = typeSymbol.get();
        } else {
            return new BoundErrorExpression();
        }

        Expression member = memberAccessorExpression.getMember();
        if (member.getExpressionType() == ExpressionType.FUNC_CALL_EXPR) {
            //UFCS https://en.wikipedia.org/wiki/Uniform_Function_Call_Syntax
            FunctionCallExpression functionCallExpression = (FunctionCallExpression) member;

            Optional<FunctionSymbol> function = currentScope.tryLookupFunction((String) functionCallExpression.getIdentifier().getValue());
            IdentifierExpression dummyRefKeyword = null;
            if (function.isPresent()) {
                BoundFunctionParameterExpression firstArg = function.get().getArguments().get(0);
                if (firstArg.isReference()) {
                    if (memberAccessorExpression.getAccessor().getTokenType() == TokenType.DOT) {
                        errors.add(BindingError.raise("Receiver of function `" + function.get().getSignature() + "` must be accessed by reference. Perhaps you meant to use `->` instead of `.`", memberAccessorExpression.getAccessor().getSpan()));
                        return new BoundErrorExpression();
                    }
                    //Add the dummy anyway to avoid irrelevant errors
                    Location dummyLocation = Location.fromOffset(
                            functionCallExpression.getIdentifier().getSpan().getStart(),
                            //This looks ridiculous, but it's calculating the start of the member owner
                            //minus the dot     |                minus the function identifier              |                                                    minus the length of the owner
                            -1 - ((String) functionCallExpression.getIdentifier().getValue()).length() - (memberAccessorExpression.getOwner().getSpan().getEnd().getColumn() - memberAccessorExpression.getOwner().getSpan().getStart().getColumn()));
                    dummyRefKeyword = new IdentifierExpression(new Token(TokenType.REF_KEYWORD, dummyLocation), TokenType.REF_KEYWORD, "ref");
                } else if (memberAccessorExpression.getAccessor().getTokenType() == TokenType.ARROW) {
                    errors.add(BindingError.raise("Receiver of function `" + function.get().getSignature() + "` must be accessed by value. Perhaps you meant to use `.` instead of `->`", memberAccessorExpression.getAccessor().getSpan()));
                    return new BoundErrorExpression();
                }
            }

            List<FunctionCallArgumentExpression> ufscArgs = new ArrayList<>();
            ufscArgs.add(new FunctionCallArgumentExpression(dummyRefKeyword, memberAccessorExpression.getOwner()));
            ufscArgs.addAll(functionCallExpression.getArguments());

            FunctionCallExpression ufcsFunctionCallExpression = new FunctionCallExpression(
                    functionCallExpression.getIdentifier(),
                    functionCallExpression.getOpenParen(),
                    ufscArgs,
                    functionCallExpression.getCloseParen()
            );
            return bindFunctionCallExpression(ufcsFunctionCallExpression);
        }

        if (member.getExpressionType() == ExpressionType.IDENTIFIER_EXPR) {

            IdentifierExpression identifier = (IdentifierExpression) member;
            VariableSymbol variable = type.getFields().get(identifier.getValue());
            if (!type.getFields().containsKey(identifier.getValue())) {
                errors.add(BindingError.raiseUnknownMember((String) identifier.getValue(), type, member.getSpan()));
                return new BoundErrorExpression();
            }
            BoundVariableExpression variableExpression = new BoundVariableExpression(variable);
            return new BoundMemberAccessorExpression(boundOwner, variableExpression);
        }
        errors.add(BindingError.raise("Unknown member for type `" + type + "`", member.getSpan()));
        return new BoundErrorExpression();
    }

    private BoundExpression bindYieldExpression(YieldExpression yieldExpression) {
        BoundExpression expression = bind(yieldExpression.getExpression());

        return new BoundYieldExpression(expression);
    }

    private BoundExpression bindArrayDeclarationExpression(ArrayDeclarationExpression arrayDeclarationExpression) {
        BoundExpression elementCount = bind(arrayDeclarationExpression.getElementCount());
        if (!elementCount.getType().isAssignableFrom(INT)) {
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
                errors.add(BindingError.raiseTypeMismatch(type, boundMatchCaseExpression.getType(), caseExpression.getThenExpression().getSpan()));
                return new BoundErrorExpression();
            }

            type = boundMatchCaseExpression.getType();
            if (boundMatchCaseExpression.getCaseExpression() != null
                    && !operand.getType().isAssignableFrom(boundMatchCaseExpression.getCaseExpression().getType())
                    && boundMatchCaseExpression.getCaseExpression().getType() != TypeSymbol.BOOL) {
                errors.add(BindingError.raiseTypeMismatch(operand.getType(), boundMatchCaseExpression.getCaseExpression().getType(), caseExpression.getCaseExpression().getSpan()));
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
        TypeSymbol type = null;
        List<Expression> elements = arrayLiteralExpression.getElements();
        for (int i = 0; i < elements.size(); i++) {
            Expression element = elements.get(i);
            BoundExpression boundElement = bind(element);
            if (type == null) {
                type = boundElement.getType();
            } else {
                if (!type.isAssignableFrom(boundElement.getType())) {
                    errors.add(BindingError.raiseTypeMismatch(type, boundElement.getType(), arrayLiteralExpression.getElements().get(i).getSpan()));
                }
            }
            boundElements.add(boundElement);
        }

        return new BoundArrayLiteralExpression(boundElements);
    }

    private BoundPositionalAccessExpression bindArrayAccessExpression(ArrayAccessExpression arrayAccessExpression) {
        IdentifierExpression identifier = arrayAccessExpression.getIdentifier();
        Optional<VariableSymbol> variable = currentScope.tryLookupVariable((String) identifier.getValue());
        if (variable.isEmpty()) {
            throw new UndefinedVariableException((String) identifier.getValue());
        }

        BoundExpression index = bind(arrayAccessExpression.getIndex());

        if (!index.getType().isAssignableFrom(INT)) {
            errors.add(BindingError.raiseTypeMismatch(INT, index.getType(), arrayAccessExpression.getIndex().getSpan()));
        }
        return new BoundPositionalAccessExpression(new BoundVariableExpression(variable.get()), index);
    }

    private BoundExpression bindArrayLengthExpression(ArrayLengthExpression arrayLengthExpression) {

        BoundExpression boundExpression = bind(arrayLengthExpression.getExpression());
//        if (!boundExpression.getType().isAssignableFrom(TypeSymbol.INT_ARRAY) && boundExpression.getType() != TypeSymbol.TUPLE) {
//            errors.add(Error.raiseTypeMismatch(TypeSymbol.INT_ARRAY, boundExpression.getType()));
//        }

        return new BoundArrayLengthExpression(boundExpression);
    }

    private BoundExpression bindArrayAssignmentExpression(ArrayAssignmentExpression arrayAssignmentExpression) {

        BoundPositionalAccessExpression boundArrayAccessExpression = bindArrayAccessExpression(arrayAssignmentExpression.getArrayAccessExpression());

        BoundExpression array = boundArrayAccessExpression.getArray();
        if (array.getType() instanceof TupleTypeSymbol) {
            errors.add(BindingError.raise("Type `TupleTypeSymbol` is immutable and does not support member reassignment", arrayAssignmentExpression.getSpan()));
        }

        BoundExpression assignment = bind(arrayAssignmentExpression.getAssignment());

        return new BoundArrayAssignmentExpression(boundArrayAccessExpression, assignment);
    }

    private BoundExpression bindMemberAssignmentExpression(MemberAssignmentExpression memberAssignmentExpression) {

        BoundExpression boundExpression = bindMemberAccessorExpression(memberAssignmentExpression.getMemberAccessorExpression());
        if (boundExpression.getBoundExpressionType() != BoundExpressionType.MEMBER_ACCESSOR) {
            return new BoundErrorExpression();
        }
        BoundMemberAccessorExpression boundMemberAccessorExpression = (BoundMemberAccessorExpression) boundExpression;
        if (boundMemberAccessorExpression.getOwner().isConstExpression()) {
            errors.add(BindingError.raise("Member belongs to const expression, cannot be modified", memberAssignmentExpression.getMemberAccessorExpression().getSpan()));
        }

        BoundExpression assignment = bind(memberAssignmentExpression.getAssignment());

        if (!boundMemberAccessorExpression.getMember().getType().isAssignableFrom(assignment.getType())) {
            errors.add(BindingError.raiseTypeMismatch(boundMemberAccessorExpression.getMember().getType(), assignment.getType(), memberAssignmentExpression.getAssignment().getSpan()));
        }

        if (boundMemberAccessorExpression.getMember() instanceof BoundVariableExpression) { //Always true right now
            BoundVariableExpression variableExpression = (BoundVariableExpression) boundMemberAccessorExpression.getMember();
            if (variableExpression.getVariable().isReadOnly()) {
                errors.add(BindingError.raiseConstReassignmentError(variableExpression.getVariable(), memberAssignmentExpression.getSpan()));
                return new BoundMemberAssignmentExpression(boundMemberAccessorExpression, assignment);
            }
        }

        return new BoundMemberAssignmentExpression(boundMemberAccessorExpression, assignment);
    }

    private BoundExpression bindForExpression(ForExpression forExpression) {

        currentScope = new BoundScope(currentScope);
        BoundExpression boundRangeExpression = bindRangeExpression(forExpression.getRangeExpression());
        if (!(boundRangeExpression instanceof BoundRangeExpression)) {
            return boundRangeExpression;
        }
        BoundRangeExpression range = (BoundRangeExpression) boundRangeExpression;

        TypeSymbol type = parseType(forExpression.getTypeExpression());

        if (!type.isAssignableFrom(range.getType())) {
            errors.add(BindingError.raiseTypeMismatch(type, range.getType(), forExpression.getRangeExpression().getSpan()));
        }

        VariableSymbol variable = buildVariableSymbol(type, forExpression.getIdentifier(), null, true, forExpression);

        try {
            currentScope.declareVariable((String) forExpression.getIdentifier().getValue(), variable);
        } catch (VariableAlreadyDeclaredException vade) {
            VariableSymbol alreadyDeclaredVariable = currentScope.tryLookupVariable((String) forExpression.getIdentifier().getValue()).get();
            errors.add(BindingError.raiseVariableAlreadyDeclared(alreadyDeclaredVariable, forExpression.getIdentifier().getSpan(), alreadyDeclaredVariable.getDeclaration().getSpan()));
        }
        BoundExpression guard = null;
        if (forExpression.getGuard() != null) {
            guard = bind(forExpression.getGuard());
        }
        BoundExpression body = bind(forExpression.getBody());

        currentScope = currentScope.getParentScope();

        return new BoundForExpression(variable, range, guard, body);
    }

    private BoundExpression bindRangeExpression(RangeExpression rangeExpression) {

        BoundExpression lowerBound = bind(rangeExpression.getLowerBound());
        BoundExpression upperBound = bind(rangeExpression.getUpperBound());

        BoundExpression step = null;
        if (rangeExpression.getStep() != null) {
            step = bind(rangeExpression.getStep());
        }

        try {
            return new BoundRangeExpression(lowerBound, upperBound, step);
        } catch (TypeMismatchException tme) {
            errors.add(BindingError.raiseTypeMismatch(lowerBound.getType(), upperBound.getType(), rangeExpression.getSpan()));
            return new BoundErrorExpression();
        }
    }

    private BoundExpression bindForInExpression(ForInExpression forInExpression) {
        currentScope = new BoundScope(currentScope);

        BoundExpression iterable = bind(forInExpression.getIterable());

        TypeSymbol type = parseType(forInExpression.getTypeExpression());

        if (!(iterable.getType() instanceof ArrayTypeSymbol)) {
            errors.add(BindingError.raiseTypeMismatch(new ArrayTypeSymbol(type), iterable.getType(), forInExpression.getIterable().getSpan()));
            return new BoundErrorExpression();
        }
        if (!type.isAssignableFrom(((ArrayTypeSymbol) iterable.getType()).getType())) {
            errors.add(BindingError.raiseTypeMismatch(new ArrayTypeSymbol(type), iterable.getType(), forInExpression.getIterable().getSpan()));
        }
        VariableSymbol variable = buildVariableSymbol(type, forInExpression.getIdentifier(), null, false, forInExpression);

        try {
            currentScope.declareVariable((String) forInExpression.getIdentifier().getValue(), variable);
        } catch (VariableAlreadyDeclaredException vade) {
            VariableSymbol alreadyDeclaredVariable = currentScope.tryLookupVariable((String) forInExpression.getIdentifier().getValue()).get();
            errors.add(BindingError.raiseVariableAlreadyDeclared(alreadyDeclaredVariable, forInExpression.getIdentifier().getSpan(), alreadyDeclaredVariable.getDeclaration().getSpan()));
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
        if (typeExpression == null) {
            return TypeSymbol.UNIT;
        }
        if (typeExpression.getIdentifier() instanceof TupleTypeExpression) {
            List<TypeSymbol> boundTypes = ((TupleTypeExpression) typeExpression.getIdentifier()).getTypeExpressions()
                    .stream()
                    .map(DelimitedExpression::getExpression)
                    .map(this::parseType)
                    .collect(Collectors.toList());
            typeSymbol = new TupleTypeSymbol(boundTypes);
        } else {
            IdentifierExpression identifier = (IdentifierExpression) typeExpression.getIdentifier();

            typeSymbol = getTypeSymbol(identifier);
        }

        //TODO: This doesn't do N-Dimensional arrays yet
        if (typeExpression.getOpenSquareBracket() != null && typeExpression.getCloseSquareBracket() != null) {
            return new ArrayTypeSymbol(typeSymbol);
        }
        return typeSymbol;
    }

    private TypeSymbol getTypeSymbol(IdentifierExpression identifier) {
        TypeSymbol typeSymbol;
        switch (identifier.getTokenType()) {
            case CHAR_KEYWORD:
                typeSymbol = TypeSymbol.CHAR;
                break;
            case INT_KEYWORD:
                typeSymbol = INT;
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
            case ANY_KEYWORD:
                typeSymbol = TypeSymbol.ANY;
                break;
            default:
                Optional<TypeSymbol> type = currentScope.tryLookupType((String) identifier.getValue());
                if (type.isEmpty()) {
                    errors.add(BindingError.raiseUnknownType((String) identifier.getValue(), identifier.getSpan()));
                    typeSymbol = null;
                } else {
                    typeSymbol = type.get();
                }
        }
        return typeSymbol;
    }

    private VariableSymbol buildVariableSymbol(TypeSymbol type, IdentifierExpression identifier, BoundExpression guard, boolean readOnly, Expression declaration) {
        return new VariableSymbol((String) identifier.getValue(), type, guard, readOnly, declaration);
    }

    private BoundExpression bindIfExpression(IfExpression ifExpression) {

        BoundExpression condition = bind(ifExpression.getCondition());
        if (!condition.getType().isAssignableFrom(TypeSymbol.BOOL)) {
            errors.add(BindingError.raiseTypeMismatch(TypeSymbol.BOOL, condition.getType(), ifExpression.getCondition().getSpan()));
        }
        BoundExpression body = bind(ifExpression.getBody());

        BoundExpression elseBody = null;
        if (ifExpression.getElseBody() != null) {
            elseBody = bind(ifExpression.getElseBody());
        }
        return new BoundIfExpression(condition, body, elseBody);
    }

    private BoundExpression bindUnaryExpression(UnaryExpression unaryExpression) {
        BoundExpression operand = bind(unaryExpression.getOperand());

        try {
            BoundUnaryOperator operator = BoundUnaryOperator.bind(unaryExpression.getOperator(), operand.getType());
            return new BoundUnaryExpression(operator, operand);
        } catch (InvalidOperationException ioe) {
            errors.add(BindingError.raiseInvalidOperationException(unaryExpression.getOperator(), operand.getType(), unaryExpression.getSpan()));
            return new BoundUnaryExpression(BoundUnaryOperator.error(unaryExpression.getOperator(), operand.getType()), operand);
        }
    }

    private BoundExpression bindBinaryExpression(BinaryExpression binaryExpression) {

        BoundExpression left = bind(binaryExpression.getLeft());
        BoundExpression right = bind(binaryExpression.getRight());

        try {
            BoundBinaryOperator operator;
            if (left.getType() instanceof EnumTypeSymbol && right.getType() instanceof EnumTypeSymbol && left.getType().getName().equals(right.getType().getName())) {
                operator = BoundBinaryOperator.bind(binaryExpression.getOperation(), INT, INT);
            } else {
                operator = BoundBinaryOperator.bind(binaryExpression.getOperation(), left.getType(), right.getType());
            }

            //TODO: This is hella broken for const variables
//            if (left.isConstExpression() && left.getType() == TypeSymbol.INT && right.isConstExpression() && right.getType() == TypeSymbol.INT) {
//
//                return calculateConstantExpression((int) left.getConstValue(), operator, (int) right.getConstValue());
//            } else if (left.isConstExpression() && left.getType() == TypeSymbol.BOOL && right.isConstExpression() && right.getType() == TypeSymbol.BOOL) {
//
//                return calculateConstantExpression((boolean) left.getConstValue(), operator, (boolean) right.getConstValue());
//            } else if (left.isConstExpression() && left.getType() == TypeSymbol.REAL && right.isConstExpression() && right.getType() == TypeSymbol.REAL) {
//
//                return calculateConstantExpression((double) left.getConstValue(), operator, (double) right.getConstValue());
//            } else if (left.isConstExpression() && left.getType() == TypeSymbol.INT && right.isConstExpression() && right.getType() == TypeSymbol.REAL) {
//
//                return calculateConstantExpression(Integer.valueOf((int) left.getConstValue()).doubleValue(), operator, (double) right.getConstValue());
//            } else if (left.isConstExpression() && left.getType() == TypeSymbol.REAL && right.isConstExpression() && right.getType() == TypeSymbol.INT) {
//
//                return calculateConstantExpression((double) left.getConstValue(), operator, Integer.valueOf((int) right.getConstValue()).doubleValue());
//            }

            return new BoundBinaryExpression(left, operator, right);
        } catch (InvalidOperationException ioe) {
            errors.add(BindingError.raiseInvalidOperationException(binaryExpression.getOperation(), left.getType(), right.getType(), binaryExpression.getSpan()));
            return new BoundBinaryExpression(left, BoundBinaryOperator.error(binaryExpression.getOperation(), left.getType(), right.getType()), right);
        }
    }

    private BoundExpression calculateConstantExpression(double left, BoundBinaryOperator operator, double right) {

        switch (operator.getBoundOpType()) {
            case ADDITION:
                return new BoundLiteralExpression(left + right);
            case SUBTRACTION:
                return new BoundLiteralExpression(left - right);
            case MULTIPLICATION:
                return new BoundLiteralExpression(left * right);
            case DIVISION:
                return new BoundLiteralExpression(left / right);
            case REMAINDER:
                return new BoundLiteralExpression(left % right);
            case GREATER_THAN:
                return new BoundLiteralExpression(left > right);
            case LESS_THAN:
                return new BoundLiteralExpression(left < right);
            case GREATER_THAN_OR_EQUAL:
                return new BoundLiteralExpression(left >= right);
            case LESS_THAN_OR_EQUAL:
                return new BoundLiteralExpression(left <= right);
            case EQUALS:
                return new BoundLiteralExpression(left == right);
            case NOT_EQUALS:
                return new BoundLiteralExpression(left != right);
            case ERROR:
                return new BoundBinaryExpression(new BoundLiteralExpression(left), operator, new BoundLiteralExpression(right));
            default:
                throw new IllegalStateException("Unhandled binary expression for real const evaluation: " + operator.getBoundOpType());
        }
    }

    private BoundExpression calculateConstantExpression(int left, BoundBinaryOperator operator, int right) {

        switch (operator.getBoundOpType()) {
            case ADDITION:
                return new BoundLiteralExpression(left + right);
            case SUBTRACTION:
                return new BoundLiteralExpression(left - right);
            case MULTIPLICATION:
                return new BoundLiteralExpression(left * right);
            case DIVISION:
                return new BoundLiteralExpression(left / right);
            case REMAINDER:
                return new BoundLiteralExpression(left % right);
            case GREATER_THAN:
                return new BoundLiteralExpression(left > right);
            case LESS_THAN:
                return new BoundLiteralExpression(left < right);
            case GREATER_THAN_OR_EQUAL:
                return new BoundLiteralExpression(left >= right);
            case LESS_THAN_OR_EQUAL:
                return new BoundLiteralExpression(left <= right);
            case EQUALS:
                return new BoundLiteralExpression(left == right);
            case NOT_EQUALS:
                return new BoundLiteralExpression(left != right);
            case ERROR:
                return new BoundBinaryExpression(new BoundLiteralExpression(left), operator, new BoundLiteralExpression(right));
            default:
                throw new IllegalStateException("Unhandled binary expression for int const evaluation: " + operator.getBoundOpType());
        }
    }

    private BoundExpression calculateConstantExpression(boolean left, BoundBinaryOperator operator, boolean right) {
        switch (operator.getBoundOpType()) {
            case BOOLEAN_OR:
                return new BoundLiteralExpression(left || right);
            case BOOLEAN_AND:
                return new BoundLiteralExpression(left && right);
            case BOOLEAN_XOR:
                return new BoundLiteralExpression(left ^ right);
            case ERROR:
                return new BoundBinaryExpression(new BoundLiteralExpression(left), operator, new BoundLiteralExpression(right));
            default:
                throw new IllegalStateException("Unhandled binary expression for bool const evaluation: " + operator.getBoundOpType());
        }
    }

    private BoundExpression bindIdentifierExpression(IdentifierExpression identifierExpression) {

        if (identifierExpression.getTokenType() == TokenType.NUM_LITERAL
                || identifierExpression.getTokenType() == TokenType.STRING_LITERAL
                || identifierExpression.getTokenType() == TokenType.CHAR_LITERAL) {
            return new BoundLiteralExpression(identifierExpression.getValue());

        } else if (identifierExpression.getTokenType() == TokenType.TRUE_KEYWORD) {
            return new BoundLiteralExpression(true);

        } else if (identifierExpression.getTokenType() == TokenType.FALSE_KEYWORD) {
            return new BoundLiteralExpression(false);
        }

        Optional<TypeSymbol> type = currentScope.tryLookupType((String) identifierExpression.getValue());
        if (type.isPresent()) {
            return new BoundTypeExpression(type.get());
        }

        Optional<VariableSymbol> variable = currentScope.tryLookupVariable((String) identifierExpression.getValue());
        if (variable.isPresent()) {
            return new BoundVariableExpression(variable.get());
        }
        errors.add(BindingError.raiseUnknownIdentifier((String) identifierExpression.getValue(), identifierExpression.getSpan()));
        return new BoundErrorExpression();
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

        if (boundExpression.getType() == UNIT) {
            errors.add(BindingError.raiseUnknownFunction("print", Collections.singletonList(boundExpression), printExpression.getExpression().getSpan()));
        }

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
            errors.add(BindingError.raiseUnknownIdentifier((String) identifier.getValue(), assignmentExpression.getIdentifier().getSpan()));
            return new BoundErrorExpression();
        }
        VariableSymbol variable = scopedVariable.get();
        if (variable.isReadOnly()) {
            errors.add(BindingError.raiseConstReassignmentError(variable, assignmentExpression.getSpan()));
            return new BoundAssignmentExpression(variable, variable.getGuard(), initialiser);
        }

        if (!variable.getType().isAssignableFrom(initialiser.getType())) {
            errors.add(BindingError.raiseTypeMismatch(variable.getType(), initialiser.getType(), assignmentExpression.getAssignment().getSpan()));
        }

        return new BoundAssignmentExpression(variable, variable.getGuard(), initialiser);
    }

    private BoundExpression bindStructDeclarationExpression(StructDeclarationExpression structDeclarationExpression) {

        IdentifierExpression identifier = structDeclarationExpression.getIdentifier();

        currentScope = new BoundScope(currentScope);

        List<BoundExpression> members = new ArrayList<>();
        for (Expression member : structDeclarationExpression.getMembers()) {
            members.add(bind(member));
        }

        LinkedHashMap<String, VariableSymbol> definedVariables = currentScope.getDefinedVariables();

        TypeSymbol type = new TypeSymbol((String) identifier.getValue(), definedVariables);

        currentScope = currentScope.getParentScope();

        try {
            currentScope.declareType((String) identifier.getValue(), type);
        } catch (TypeAlreadyDeclaredException tade) {
            errors.add(BindingError.raiseTypeAlreadyDeclared((String) identifier.getValue(), identifier.getSpan()));
        }

        return new BoundStructDeclarationExpression(type, members);
    }

    private BoundExpression bindEnumDeclarationExpression(EnumDeclarationExpression enumDeclarationExpression) {

        String name = (String) enumDeclarationExpression.getIdendifier().getValue();
        currentScope = new BoundScope(currentScope);

        EnumTypeSymbol type = new EnumTypeSymbol(name, new LinkedHashMap<>());
        List<VariableSymbol> members = new ArrayList<>();
        for (int i = 0; i < enumDeclarationExpression.getMembers().size(); i++) {

            //TODO: Using variables might not be the best way to handle them, they're not integers they're colours
            IdentifierExpression identifier = enumDeclarationExpression.getMembers().get(i);
            VariableSymbol member = new VariableSymbol((String) identifier.getValue(), type, null, true, identifier);
            try {
                currentScope.declareVariable(member.getName(), member);
            } catch (VariableAlreadyDeclaredException vade) {
                errors.add(BindingError.raiseVariableAlreadyDeclared(member, identifier.getSpan(), currentScope.tryLookupVariable(member.getName()).get().getDeclaration().getSpan()));
            }
            members.add(member);
        }
        LinkedHashMap<String, VariableSymbol> definedVariables = currentScope.getDefinedVariables();
        type = new EnumTypeSymbol(name, definedVariables);

        currentScope = currentScope.getParentScope();
        try {
            currentScope.declareType(name, type);
        } catch (TypeAlreadyDeclaredException tade) {
            errors.add(BindingError.raiseTypeAlreadyDeclared(name, enumDeclarationExpression.getIdendifier().getSpan()));
        }
        return new BoundEnumDeclarationExpression(type, members);
    }

    private BoundExpression bindFunctionDeclarationExpression(FunctionDeclarationExpression functionDeclarationExpression) {

        IdentifierExpression identifier = functionDeclarationExpression.getIdentifier();

        TypeSymbol type = parseType(functionDeclarationExpression.getTypeExpression());

        currentScope = new BoundScope(currentScope);

        //Declare the arguments within the function's scope
        List<BoundFunctionParameterExpression> arguments = new ArrayList<>();
        for (FunctionParameterExpression argumentExpression : functionDeclarationExpression.getArguments()) {
            arguments.add(bindFunctionArgumentExpression(argumentExpression));
        }

        FunctionSymbol functionSymbol = new FunctionSymbol((String) identifier.getValue(), type, arguments, null);
        try {
            //Declare the function in the parent scope
            currentScope.getParentScope().declareFunction((String) identifier.getValue(), functionSymbol);
        } catch (FunctionAlreadyDeclaredException fade) {
            errors.add(BindingError.raiseFunctionAlreadyDeclared((String) identifier.getValue(), identifier.getSpan()));
        }

        BoundBlockExpression body = bindBlockExpression(functionDeclarationExpression.getBody());

        errors.addAll(FunctionAnalyser.analyzeBody(functionSymbol, body.getExpressions(), functionDeclarationExpression.getBody().getExpressions(), functionDeclarationExpression));

        currentScope = currentScope.getParentScope();

        BoundFunctionDeclarationExpression boundFunctionDeclarationExpression = new BoundFunctionDeclarationExpression(functionSymbol, arguments, body);

        if (identifier.getValue().equals("main")) {
            typeCheckMainFunction(boundFunctionDeclarationExpression, functionDeclarationExpression);
        }

        return boundFunctionDeclarationExpression;
    }

    private void typeCheckMainFunction(BoundFunctionDeclarationExpression boundMainFunction, FunctionDeclarationExpression mainFunction) {

        if (boundMainFunction.getFunctionSymbol().getType() != TypeSymbol.UNIT) {
            errors.add(BindingError.raiseTypeMismatch(TypeSymbol.UNIT, boundMainFunction.getFunctionSymbol().getType(), mainFunction.getTypeExpression().getSpan()));
        }
        List<BoundFunctionParameterExpression> arguments = boundMainFunction.getArguments();
        if (!arguments.isEmpty()) {
            if (arguments.size() == 1) {
                BoundFunctionParameterExpression argumentExpression = arguments.get(0);
                if (!argumentExpression.getType().equals(new ArrayTypeSymbol(TypeSymbol.STRING))) {
                    errors.add(BindingError.raiseTypeMismatch(new ArrayTypeSymbol(TypeSymbol.STRING), argumentExpression.getType(), mainFunction.getArguments().get(0).getSpan()));
                }
            } else {
                BoundFunctionParameterExpression argumentExpression = arguments.get(0);
                if (!argumentExpression.getType().equals(new ArrayTypeSymbol(TypeSymbol.STRING))) {
                    errors.add(BindingError.raiseTypeMismatch(new ArrayTypeSymbol(TypeSymbol.STRING), argumentExpression.getType(), mainFunction.getArguments().get(0).getSpan()));
                }
                for (int i = 1; i < arguments.size(); i++) {
                    errors.add(BindingError.raiseTypeMismatch(TypeSymbol.UNIT, boundMainFunction.getArguments().get(i).getType(), mainFunction.getArguments().get(i).getSpan()));
                }
            }
        }
    }

    private BoundLambdaExpression bindLambdaExpression(LambdaExpression lambdaExpression) {

        //IdentifierExpression identifier = functionDeclarationExpression.getIdentifier();

        currentScope = new BoundScope(currentScope);

        //Declare the arguments within the function's scope
        List<BoundFunctionParameterExpression> boundArguments = new ArrayList<>();
        for (FunctionParameterExpression argumentExpression : lambdaExpression.getArgumentExpressions()) {
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
            VariableSymbol alreadyDeclaredVariable = currentScope.tryLookupVariable(anonymousFunctionIdentifier).get();
            errors.add(BindingError.raiseVariableAlreadyDeclared(alreadyDeclaredVariable, lambdaExpression.getSpan(), alreadyDeclaredVariable.getDeclaration().getSpan()));
        }

        currentScope = currentScope.getParentScope();

        return new BoundLambdaExpression(boundArguments, boundBody);
    }

    private BoundFunctionParameterExpression bindFunctionArgumentExpression(FunctionParameterExpression argumentExpression) {

        boolean reference = argumentExpression.getRefKeyword() != null;

        IdentifierExpression identifier = argumentExpression.getIdentifier();
        TypeSymbol type = parseType(argumentExpression.getTypeExpression());

        //Create placeholder
        try {
            currentScope.declareVariable((String) identifier.getValue(), new VariableSymbol((String) identifier.getValue(), type, null, false, argumentExpression));
        } catch (VariableAlreadyDeclaredException vade) {
            VariableSymbol alreadyDeclaredVariable = currentScope.tryLookupVariable((String) identifier.getValue()).get();
            errors.add(BindingError.raiseVariableAlreadyDeclared(alreadyDeclaredVariable, identifier.getSpan(), alreadyDeclaredVariable.getDeclaration().getSpan()));
        }

        BoundExpression guard = null;
        if (argumentExpression.getGuard() != null) {
            guard = bind(argumentExpression.getGuard());
            if (guard.getType() != TypeSymbol.BOOL) {
                errors.add(BindingError.raiseTypeMismatch(TypeSymbol.BOOL, guard.getType(), argumentExpression.getGuard().getSpan()));
            }
        }

        VariableSymbol argument = buildVariableSymbol(type, identifier, guard, argumentExpression.getConstKeyword() != null, argumentExpression);

        currentScope.reassignVariable((String) identifier.getValue(), argument);

        return new BoundFunctionParameterExpression(reference, argument, guard);
    }

    private BoundExpression bindFunctionCallExpression(FunctionCallExpression functionCallExpression) {

        IdentifierExpression identifier = functionCallExpression.getIdentifier();
        Optional<FunctionSymbol> scopedFunction = currentScope.tryLookupFunction((String) identifier.getValue());
        if (scopedFunction.isEmpty()) {
            //TODO: Get arguments somehow
            List<BoundExpression> boundArguments = new ArrayList<>();
            List<FunctionCallArgumentExpression> functionCallExpressionArguments = functionCallExpression.getArguments();

            for (FunctionCallArgumentExpression functionCallExpressionArgumentExpression : functionCallExpressionArguments) {
                Expression functionCallExpressionArgument = functionCallExpressionArgumentExpression.getExpression();
                if (functionCallExpressionArgument.getExpressionType() == ExpressionType.STRUCT_LITERAL_EXPRESSION) {
                    List<BoundExpression> boundMembers = new ArrayList<>();
                    for (Expression member : ((StructLiteralExpression) functionCallExpressionArgument).getMembers()) {
                        boundMembers.add(bind(member));
                    }
                    boundArguments.add(new BoundStructLiteralExpression(TypeSymbol.ANY, boundMembers));
                } else {
                    BoundExpression boundArgument = bind(functionCallExpressionArgument);
                    boundArguments.add(boundArgument);
                }
            }
            errors.add(BindingError.raiseUnknownFunction((String) identifier.getValue(), boundArguments, functionCallExpression.getSpan()));
            return new BoundErrorExpression();
        }
        FunctionSymbol function = scopedFunction.get();

        List<BoundExpression> boundArguments = new ArrayList<>();
        List<FunctionCallArgumentExpression> functionCallExpressionArguments = functionCallExpression.getArguments();
        for (int i = 0; i < functionCallExpressionArguments.size(); i++) {
            FunctionCallArgumentExpression argumentExpression = functionCallExpressionArguments.get(i);
            Expression arg = argumentExpression.getExpression();
            if (arg.getExpressionType() == ExpressionType.STRUCT_LITERAL_EXPRESSION) {

                //TODO: This is permissible for now since there is only one possible method for the name, once overloading is possible (if overloading will be possible) this will no longer work
                //errors.add(BindingError.raise("Struct literals are not allowed in function calls, please use the full constructor form instead", functionCallExpressionArguments.get(i).getSpan()));

                List<BoundExpression> boundMembers = new ArrayList<>();
                for (Expression member : ((StructLiteralExpression) arg).getMembers()) {
                    boundMembers.add(bind(member));
                }
                BoundStructLiteralExpression boundStructLiteralExpression = new BoundStructLiteralExpression(function.getArguments().get(i).getType(), boundMembers);

                boundArguments.add(boundStructLiteralExpression);
            } else {
                BoundExpression boundArgument = bind(arg);
                boundArguments.add(boundArgument);
            }
        }

        if (function.getArguments().size() != functionCallExpression.getArguments().size()) {
            errors.add(BindingError.raiseUnknownFunction((String) identifier.getValue(), boundArguments, functionCallExpression.getSpan()));
            return new BoundFunctionCallExpression(function, boundArguments);
        }

        List<BoundFunctionParameterExpression> parameters = function.getArguments();
        for (int i = 0; i < functionCallExpressionArguments.size(); i++) {
            BoundExpression boundArgument = boundArguments.get(i);
            if (!parameters.get(i).getType().isAssignableFrom(boundArgument.getType())) {
                errors.add(BindingError.raiseTypeMismatch(parameters.get(i).getType(), boundArgument.getType(), functionCallExpressionArguments.get(i).getSpan()));
            }
            boolean passByRef = functionCallExpressionArguments.get(i).getRefKeyword() != null;
            if (parameters.get(i).isReference()) {
                if (boundArgument instanceof BoundLiteralExpression || boundArgument instanceof BoundStructLiteralExpression) {
                    errors.add(BindingError.raise("Literals cannot be passed by reference:", functionCallExpressionArguments.get(i).getSpan()));
                }
                if (!passByRef) {
                    errors.add(BindingError.raise("Argument " + i + " of `" + function.getSignature() + "` must be passed with the `ref` keyword", functionCallExpressionArguments.get(i).getSpan()));
                }
            } else if (passByRef) {
                errors.add(BindingError.raise("Argument " + i + " of `" + function.getSignature() + "` is not passed with the `ref` keyword", functionCallExpressionArguments.get(i).getSpan()));
            }
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
            currentScope.declareVariable((String) identifier.getValue(), new VariableSymbol((String) identifier.getValue(), type, null, false, variableDeclarationExpression));
        } catch (VariableAlreadyDeclaredException vade) {
            VariableSymbol alreadyDeclaredVariable = currentScope.tryLookupVariable((String) identifier.getValue()).get();
            errors.add(BindingError.raiseVariableAlreadyDeclared(alreadyDeclaredVariable, identifier.getSpan(), alreadyDeclaredVariable.getDeclaration().getSpan()));
        }

        BoundExpression guard = null;
        if (variableDeclarationExpression.getGuard() != null) {
            guard = bind(variableDeclarationExpression.getGuard());
            assert guard.getType().isAssignableFrom(TypeSymbol.BOOL);
        }

        TypeSymbol type = parseType(variableDeclarationExpression.getTypeExpression());
        if (type == null) {
            if (variableDeclarationExpression.getTypeExpression().getIdentifier() instanceof TupleTypeExpression) {
                throw new UnsupportedOperationException("TupleTypeSymbol types are not yet supported in variable declarations");
            }
            IdentifierExpression typeExpression = (IdentifierExpression) variableDeclarationExpression.getTypeExpression().getIdentifier();
            errors.add(BindingError.raiseUnknownType((String) typeExpression.getValue(), variableDeclarationExpression.getTypeExpression().getSpan()));
            return new BoundErrorExpression();
        }

        if (variableDeclarationExpression.getTypeExpression() == null && initialiser != null) {
            type = initialiser.getType();
        }

        if (initialiser != null && !type.isAssignableFrom(initialiser.getType())) {
            errors.add(BindingError.raiseTypeMismatch(type, initialiser.getType(), variableDeclarationExpression.getInitialiser().getSpan()));
        }

        boolean readOnly = variableDeclarationExpression.getConstKeyword() != null;
        if (readOnly && (initialiser == null || !initialiser.isConstExpression())) {
            errors.add(BindingError.raiseNonConstAssignmentError(variableDeclarationExpression.getIdentifier(), variableDeclarationExpression.getSpan()));
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
            return new BoundFunctionDeclarationExpression(function, lambdaExpression.getArguments(), new BoundBlockExpression(
                    new BoundReturnExpression(lambdaExpression.getBody())
            ));
        }

        VariableSymbol variable = buildVariableSymbol(type, identifier, guard, readOnly, variableDeclarationExpression);

        currentScope.reassignVariable((String) identifier.getValue(), variable);
        return new BoundVariableDeclarationExpression(variable, guard, initialiser, readOnly);
    }

    private BoundExpression bindWhileExpression(WhileExpression whileExpression) {

        BoundExpression condition = bind(whileExpression.getCondition());
        if (!condition.getType().isAssignableFrom(TypeSymbol.BOOL)) {
            throw new TypeMismatchException(TypeSymbol.BOOL, condition.getType());
        }

        if (condition.isConstExpression()) {
            if ((boolean) condition.getConstValue()) {
                warnings.add(BindingWarning.raiseConditionAlwaysTrue(whileExpression.getCondition().getSpan()));
            } else {
                warnings.add(BindingWarning.raiseConditionAlwaysFalse(whileExpression.getCondition().getSpan()));
            }
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
