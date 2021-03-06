package com.skennedy.rasna.typebinding;

import com.skennedy.rasna.diagnostics.BindingError;
import com.skennedy.rasna.diagnostics.TextSpan;
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
import com.skennedy.rasna.lowering.BoundNoOpExpression;
import com.skennedy.rasna.parsing.*;
import com.skennedy.rasna.parsing.model.ExpressionType;
import com.skennedy.rasna.parsing.model.IdentifierExpression;
import com.skennedy.rasna.parsing.model.SyntaxNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.skennedy.rasna.typebinding.TypeSymbol.ANY;
import static com.skennedy.rasna.typebinding.TypeSymbol.BOOL;
import static com.skennedy.rasna.typebinding.TypeSymbol.CHAR;
import static com.skennedy.rasna.typebinding.TypeSymbol.ERROR;
import static com.skennedy.rasna.typebinding.TypeSymbol.FILE;
import static com.skennedy.rasna.typebinding.TypeSymbol.FUNCTION;
import static com.skennedy.rasna.typebinding.TypeSymbol.INT;
import static com.skennedy.rasna.typebinding.TypeSymbol.REAL;
import static com.skennedy.rasna.typebinding.TypeSymbol.STRING;
import static com.skennedy.rasna.typebinding.TypeSymbol.UNIT;
import static com.skennedy.rasna.typebinding.TypeSymbol.getPrimitives;

public class Binder {

    private List<BindingError> errors;
    private List<BindingWarning> warnings;
    private BoundScope currentScope;

    private List<BoundExpression> boundExpressions = new ArrayList<>();

    private Map<FunctionSymbol, BlockExpression> interfaceBodies;//TODO: This is merely temporary as a proof of concept

    public Binder() {
        currentScope = new BoundScope(null);
        BuiltInFunctions.getBuiltinFunctions()
                .forEach(function -> currentScope.declareFunction(buildSignature(function.getName(), function.getArguments().stream()
                        .map(BoundFunctionParameterExpression::getType)
                        .map(TypeSymbol::toString)
                        .collect(Collectors.toList())), function));

        //What a nightmare
        InterfaceTypeSymbol.getBuiltinInterfaces().stream().peek(i -> i.getSignatures().forEach(sig -> {
            List<BoundFunctionParameterExpression> functionParameterExpressions = new ArrayList<>();
            VariableSymbol self = new VariableSymbol("self", i, null, true, null);
            functionParameterExpressions.add(new BoundFunctionParameterExpression(true, self, null));
            functionParameterExpressions.addAll(sig.getFunctionParameterExpressions());

            FunctionSymbol interfaceFunction = new FunctionSymbol(sig.getIdentifier(), sig.getReturnType(), functionParameterExpressions, null);

            List<String> argumentIdentifiers = functionParameterExpressions.stream()
                    .map(BoundFunctionParameterExpression::getArgument)
                    .map(VariableSymbol::getType)
                    .map(TypeSymbol::toString)
                    .collect(Collectors.toList());
            currentScope.declareFunction(buildSignature(sig.getIdentifier(), argumentIdentifiers), interfaceFunction);
        })).forEach(i -> currentScope.declareType(i.getName(), i));

        int i = 0;
    }

    public BoundProgram bind(Program program) {

        errors = new ArrayList<>();
        warnings = new ArrayList<>();
        interfaceBodies = new HashMap<>();

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
            case ARRAY_DECLARATION_EXPR:
                return bindArrayDeclarationExpression((ArrayDeclarationExpression) expression);
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
            case INTERFACE_EXPR:
                return bindInterface((InterfaceExpression) expression);
            case FUNC_CALL_PARAM_EXPR:
                return bind(((FunctionCallArgumentExpression) expression).getExpression());
            case WITH_BLOCK_EXPR:
                return bindWithBlockExpression((WithBlockExpression) expression);
            default:
                throw new IllegalStateException("Unexpected value: " + expression.getExpressionType());
        }
    }

    private BoundExpression bindWithBlockExpression(WithBlockExpression withBlockExpression) {
        BoundExpression boundResource = bind(withBlockExpression.getResource());

        if (boundResource.getBoundExpressionType() != BoundExpressionType.VARIABLE_DECLARATION) {
            errors.add(BindingError.raise("Resource in a `with` declaration must be a variable declaration expression", withBlockExpression.getResource().getSpan()));
        }

        BoundVariableDeclarationExpression boundVariableDeclarationExpression = (BoundVariableDeclarationExpression) boundResource;
        Optional<FunctionSymbol> close = currentScope.tryLookupFunction(buildSignature("close", Collections.singletonList(boundResource.getType().getName())));
        if (close.isEmpty()) {
            errors.add(BindingError.raise("Resource `" + boundVariableDeclarationExpression.getVariable().getName() + "` in  `with` declaration must inherit the `Closable` interface", withBlockExpression.getResource().getIdentifier().getSpan()));
            return new BoundErrorExpression();
        }
        BoundBlockExpression boundBody = bindBlockExpression(withBlockExpression.getBody());

        BoundFunctionCallExpression closeCallExpression = new BoundFunctionCallExpression(close.get(), Collections.singletonList(new BoundVariableExpression(boundVariableDeclarationExpression.getVariable())));

        return new BoundWithBlockExpression(boundVariableDeclarationExpression, boundBody, closeCallExpression);
    }

    private BoundExpression bindInterface(InterfaceExpression interfaceExpression) {

        String name = (String) interfaceExpression.getIdentifier().getValue();

        InterfaceTypeSymbol tempType = new InterfaceTypeSymbol(name, new ArrayList<>());

        List<BoundFunctionSignatureExpression> boundFunctionSignatureExpressions = new ArrayList<>();
        for (FunctionSignatureExpression signatureExpression : interfaceExpression.getSignatureExpressions()) {
            BoundFunctionSignatureExpression functionSignatureExpression = bindFunctionSignatureExpression(signatureExpression);

            List<BoundFunctionParameterExpression> functionParameterExpressions = new ArrayList<>();
            VariableSymbol self = new VariableSymbol("self", tempType, null, true, null);
            functionParameterExpressions.add(new BoundFunctionParameterExpression(signatureExpression.getRefKeyword() != null, self, null));
            functionParameterExpressions.addAll(functionSignatureExpression.getFunctionParameterExpressions());

            FunctionSymbol interfaceFunction = new FunctionSymbol(functionSignatureExpression.getIdentifier(), functionSignatureExpression.getReturnType(), functionParameterExpressions, null);

            List<String> argumentIdentifiers = functionParameterExpressions.stream()
                    .map(BoundFunctionParameterExpression::getType)
                    .map(Object::toString)
                    .collect(Collectors.toList());
            currentScope.declareFunction(buildSignature((String) signatureExpression.getIdentifier().getValue(), argumentIdentifiers), interfaceFunction);

            boundFunctionSignatureExpressions.add(functionSignatureExpression);
        }

        InterfaceTypeSymbol type = new InterfaceTypeSymbol(name, boundFunctionSignatureExpressions);

        try {
            currentScope.declareType(name, type);
        } catch (TypeAlreadyDeclaredException tade) {
            errors.add(BindingError.raiseTypeAlreadyDeclared(name, interfaceExpression.getIdentifier().getSpan()));
        }

        return new BoundNoOpExpression();
    }

    private BoundFunctionSignatureExpression bindFunctionSignatureExpression(FunctionSignatureExpression signatureExpression) {

        currentScope = new BoundScope(currentScope);

        String identifier = (String) signatureExpression.getIdentifier().getValue();

        List<BoundFunctionParameterExpression> boundFunctionParameterExpressions = new ArrayList<>();
        for (FunctionParameterExpression argumentExpression : signatureExpression.getArgumentExpressions()) {

            TypeSymbol typeSymbol = getTypeSymbol(getTypeIdentifier(argumentExpression.getTypeExpression()));
            String name = argumentExpression.getIdentifier().getValue().toString();
            BoundExpression guard = argumentExpression.getGuard() == null ? null : bind(argumentExpression.getGuard());
            VariableSymbol variableSymbol = new VariableSymbol(name, typeSymbol, guard, argumentExpression.getConstKeyword() != null, argumentExpression);
            try {
                currentScope.declareVariable(name, variableSymbol);
            } catch (VariableAlreadyDeclaredException vade) {
                VariableSymbol alreadyDeclaredVariable = currentScope.tryLookupVariable(name).get();
                errors.add(BindingError.raiseVariableAlreadyDeclared(alreadyDeclaredVariable, argumentExpression.getSpan(), alreadyDeclaredVariable.getDeclaration().getSpan()));
            }
            boundFunctionParameterExpressions.add(new BoundFunctionParameterExpression(argumentExpression.getRefKeyword() != null, variableSymbol, guard));
        }
        TypeSymbol returnType = getTypeSymbol(getTypeIdentifier(signatureExpression.getTypeExpression()));

        currentScope = currentScope.getParentScope();

        return new BoundFunctionSignatureExpression(identifier, boundFunctionParameterExpressions, returnType);
    }

    private BoundExpression bindTupleIndexExpression(TupleIndexExpression tupleIndexExpression) {
        BoundExpression boundTuple = bind(tupleIndexExpression.getTuple());
        if (!(boundTuple.getType() instanceof TupleTypeSymbol)) {
            errors.add(BindingError.raise("Expected a tuple type but got `" + boundTuple.getType() + "` instead", tupleIndexExpression.getTuple().getSpan()));
            return new BoundErrorExpression();
        }
        BoundLiteralExpression boundIndex = new BoundLiteralExpression(tupleIndexExpression.getIndex().getValue());
        if (boundIndex.getType() == REAL) {
            errors.add(BindingError.raise("Expected type `Int` but got `Real`. Did you mean to access a nested tuple? If so wrap the index in parentheses, i.e. `(t.0).0)`", tupleIndexExpression.getIndex().getSpan()));
            return new BoundErrorExpression();
        }
        if (!(tupleIndexExpression.getIndex().getValue() instanceof Integer)) {
            errors.add(BindingError.raiseTypeMismatch(INT, boundIndex.getType(), tupleIndexExpression.getIndex().getSpan()));
            return new BoundErrorExpression();
        }
        if (((TupleTypeSymbol) boundTuple.getType()).getTypes().size() <= (int) tupleIndexExpression.getIndex().getValue()) {
            errors.add(BindingError.raiseOutOfBounds((int) tupleIndexExpression.getIndex().getValue(), (TupleTypeSymbol) boundTuple.getType(), tupleIndexExpression.getSpan()));
        }
        return new BoundTupleIndexExpression(boundTuple, boundIndex);
    }

    private BoundExpression bindTypeTestExpression(TypeTestExpression typeTestExpression) {
        BoundExpression expression = bind(typeTestExpression.getExpression());
        IdentifierExpression typeLiteral = typeTestExpression.getTypeLiteral();

        return new BoundTypeTestExpression(expression, getTypeSymbol(typeLiteral));
    }

    private BoundExpression bindStructLiteralExpression(StructLiteralExpression structLiteralExpression) {

        if (structLiteralExpression.getTypeExpression().getTypeExpression() instanceof TupleTypeExpression) {
            throw new UnsupportedOperationException("TupleTypeSymbol types are not yet supported in struct literals");
        }
        if (structLiteralExpression.getTypeExpression().getTypeExpression() instanceof ErasedParameterisedTypeExpression) {
            throw new UnsupportedOperationException("Erased parameterised structs are not yet implemented");
        }
        IdentifierExpression typeExpression = (IdentifierExpression) structLiteralExpression.getTypeExpression().getTypeExpression();

        Optional<TypeSymbol> optionalTypeSymbol = currentScope.tryLookupType((String) typeExpression.getValue());
        if (optionalTypeSymbol.isEmpty()) {
            //This should never happen
            errors.add(BindingError.raiseUnknownType((String) typeExpression.getValue(), structLiteralExpression.getSpan()));
            return new BoundErrorExpression();
        }
        TypeSymbol type = optionalTypeSymbol.get();

        List<BoundExpression> boundArgs = new ArrayList<>();
        List<Expression> structLiteralExpressionMembers = structLiteralExpression.getMembers();
        List<VariableSymbol> members = new ArrayList<>(type.getFields().values());
        for (int i = 0; i < structLiteralExpressionMembers.size(); i++) {

            Expression arg = structLiteralExpressionMembers.get(i);
            BoundExpression boundArg = bind(arg);
            boundArg = typeCheck(members.get(i).getType(), boundArg, arg.getSpan());

            boundArgs.add(boundArg);
        }

        List<VariableSymbol> values = new ArrayList<>(type.getFields().values());
        if (values.size() != structLiteralExpression.getMembers().size()) {
            errors.add(BindingError.raiseUnknownStruct((String) typeExpression.getValue(), boundArgs, structLiteralExpression.getSpan()));
            return new BoundErrorExpression();
        }

        if (values.size() != boundArgs.size()) {
            //This could be a little misleading, need signature info in the functionsymbol
            errors.add(BindingError.raiseUnknownFunction((String) typeExpression.getValue(), boundArgs, structLiteralExpression.getSpan()));
            return new BoundErrorExpression();
        }

        if (type instanceof ParameterisedTypeSymbol) {
            ErasedParameterisedTypeSymbol erasedType = eraseParameters(structLiteralExpression, (ParameterisedTypeSymbol) type, boundArgs, values);
            if (currentScope.tryLookupType(erasedType.getName()).isEmpty()) {
                currentScope.declareType(erasedType.getName(), erasedType);
            }
            return new BoundStructLiteralExpression(erasedType, boundArgs);
        }

        return new BoundStructLiteralExpression(type, boundArgs);
    }

    private ErasedParameterisedTypeSymbol eraseParameters(StructLiteralExpression structLiteralExpression, ParameterisedTypeSymbol type, List<BoundExpression> literalMembers, List<VariableSymbol> structMembers) {
        Map<String, TypeSymbol> erasures = new HashMap<>();

        List<String> genericParameters = type.getGenericParameters();

        for (int i = 0; i < literalMembers.size(); i++) {
            BoundExpression boundMember = literalMembers.get(i);
            TypeSymbol expectedType = structMembers.get(i).getType();
            if (!expectedType.isAssignableFrom(boundMember.getType())) {

                if (erasures.containsKey(expectedType.getName())) {
                    literalMembers.set(i, typeCheck(erasures.get(expectedType.getName()), boundMember, structLiteralExpression.getMembers().get(i).getSpan()));
                    //TODO: If the bound type is composite (i.e. <T> -> T[]) it is not erased correctly
                } else if (genericParameters.contains(expectedType.getName())) {
                    erasures.put(expectedType.getName(), getBaseType(boundMember.getType()));
                }
            }
        }
        return new ErasedParameterisedTypeSymbol(type.getName(), new LinkedHashMap<>(type.getFields()), erasures);
    }

    private TypeSymbol getBaseType(TypeSymbol type) {
        if (type instanceof ArrayTypeSymbol) {
            return getBaseType(((ArrayTypeSymbol) type).getType());
        }
        return type;
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
        if (boundOwner instanceof BoundErrorExpression || boundOwner.getType() == ERROR) {
            return boundOwner;
        }

        TypeSymbol type;
        if (boundOwner instanceof BoundTypeExpression) {
            type = ((BoundTypeExpression) boundOwner).getTypeSymbol();
        } else if (getPrimitives().contains(boundOwner.getType()) || boundOwner.getType() instanceof TupleTypeSymbol) {
            type = boundOwner.getType();
        } else if (boundOwner.getType() instanceof ArrayTypeSymbol) {
            type = boundOwner.getType();
        } else if (boundOwner.getType() != null) {
            Optional<TypeSymbol> typeSymbol = currentScope.tryLookupType(boundOwner.getType().getName());
            if (typeSymbol.isEmpty()) {
                throw new IllegalStateException("No such type " + boundOwner.getType() + " in scope, possibly a parser bug");
            }
            type = typeSymbol.get();
        } else {
            return new BoundErrorExpression();
        }

        Expression member = memberAccessorExpression.getMember();
        if (member.getExpressionType() == ExpressionType.FUNC_CALL_EXPR) {
            //UFCS https://en.wikipedia.org/wiki/Uniform_Function_Call_Syntax
            FunctionCallExpression functionCallExpression = (FunctionCallExpression) member;


            List<BoundExpression> boundArguments = new ArrayList<>();
            boundArguments.add(boundOwner);
            functionCallExpression.getArguments().stream()
                    .map(this::bind).forEach(boundArguments::add);

            List<String> argumentTypes = new ArrayList<>();
            boundArguments.stream()
                    .map(BoundExpression::getType)
                    .map(TypeSymbol::getName)
                    .forEach(argumentTypes::add);

            String identifier = (String) functionCallExpression.getIdentifier().getValue();
            Optional<FunctionSymbol> function = currentScope.tryLookupFunction(buildSignature(identifier, argumentTypes));
            IdentifierExpression dummyRefKeyword = null;
            if (function.isEmpty()) {
                Set<FunctionSymbol> potentialFunctions = currentScope.tryLookupInterfaceFunctions(identifier);
                if (potentialFunctions.isEmpty()) {
                    errors.add(BindingError.raiseUnknownFunction(identifier, boundArguments, functionCallExpression.getSpan()));
                    return new BoundErrorExpression();
                }

                List<FunctionSymbol> compatibleFunctions = potentialFunctions.stream()
                        .filter(func -> isCompatible(boundArguments, func))
                        .collect(Collectors.toList());
                if (compatibleFunctions.isEmpty()) {
                    errors.add(BindingError.raiseUnknownFunction(identifier, boundArguments, functionCallExpression.getSpan()));
                    return new BoundErrorExpression();
                }
                if (compatibleFunctions.size() > 1) {
                    throw new IllegalStateException("More than one function with matching signature found");
                }

                function = Optional.of(compatibleFunctions.get(0));
            }

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
                        -1 - (identifier).length() - (memberAccessorExpression.getOwner().getSpan().getEnd().getColumn() - memberAccessorExpression.getOwner().getSpan().getStart().getColumn()));
                dummyRefKeyword = new IdentifierExpression(new Token(TokenType.REF_KEYWORD, dummyLocation), TokenType.REF_KEYWORD, "ref");
            } else if (memberAccessorExpression.getAccessor().getTokenType() == TokenType.ARROW) {
                errors.add(BindingError.raise("Receiver of function `" + function.get().getSignature() + "` must be accessed by value. Perhaps you meant to use `.` instead of `->`", memberAccessorExpression.getAccessor().getSpan()));
                return new BoundErrorExpression();
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

    private BoundExpression bindArrayDeclarationExpression(ArrayDeclarationExpression arrayDeclarationExpression) {
        BoundExpression elementCount = bind(arrayDeclarationExpression.getElementCount());

        elementCount = typeCheck(INT, elementCount, arrayDeclarationExpression.getElementCount().getSpan());

        if (!elementCount.isConstExpression()) {
            throw new IllegalStateException("Element count must be a const int"); //TODO: Raise a proper error
        }
        ArrayTypeSymbol typeSymbol = new ArrayTypeSymbol(parseType(arrayDeclarationExpression.getTypeExpression()));

        return new BoundArrayDeclarationExpression(typeSymbol, elementCount);
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
                    && boundMatchCaseExpression.getCaseExpression().getType() != BOOL) {
                errors.add(BindingError.raiseTypeMismatch(operand.getType(), boundMatchCaseExpression.getCaseExpression().getType(), caseExpression.getCaseExpression().getSpan()));
            }
            boundMatchCaseExpressions.add(boundMatchCaseExpression);
        }

        //Ensure exhaustiveness of the match statement
        BoundMatchExpression boundMatchExpression = new BoundMatchExpression(type, operand, boundMatchCaseExpressions);
        errors.addAll(MatchAnalyser.analyse(boundMatchExpression, matchExpression, currentScope));

        return boundMatchExpression;
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
                boundElement = typeCheck(type, boundElement, arrayLiteralExpression.getElements().get(i).getSpan());
            }
            boundElements.add(boundElement);
        }

        return new BoundArrayLiteralExpression(boundElements);
    }

    private BoundExpression bindArrayAccessExpression(ArrayAccessExpression arrayAccessExpression) {
        BoundExpression array = bind(arrayAccessExpression.getArray());
        if (!(array.getType() instanceof ArrayTypeSymbol)) {
            errors.add(BindingError.raiseTypeMismatch(new ArrayTypeSymbol(array.getType()), array.getType(), arrayAccessExpression.getIndex().getSpan()));
        }

        BoundExpression index = bind(arrayAccessExpression.getIndex());

        if (!index.getType().isAssignableFrom(INT)) {
            errors.add(BindingError.raiseTypeMismatch(INT, index.getType(), arrayAccessExpression.getIndex().getSpan()));
        }
        return new BoundPositionalAccessExpression(array, index);
    }

    private BoundExpression bindArrayLengthExpression(ArrayLengthExpression arrayLengthExpression) {

        BoundExpression boundExpression = bind(arrayLengthExpression.getExpression());
//        if (!boundExpression.getType().isAssignableFrom(TypeSymbol.INT_ARRAY) && boundExpression.getType() != TypeSymbol.TUPLE) {
//            errors.add(Error.raiseTypeMismatch(TypeSymbol.INT_ARRAY, boundExpression.getType()));
//        }

        return new BoundArrayLengthExpression(boundExpression);
    }

    private BoundExpression bindArrayAssignmentExpression(ArrayAssignmentExpression arrayAssignmentExpression) {

        BoundExpression boundExpression = bindArrayAccessExpression(arrayAssignmentExpression.getArrayAccessExpression());
        if (!(boundExpression instanceof BoundPositionalAccessExpression)) {
            return boundExpression;
        }
        BoundPositionalAccessExpression boundArrayAccessExpression = (BoundPositionalAccessExpression) boundExpression;

        BoundExpression array = boundArrayAccessExpression.getArray();
        if (array.getType() instanceof TupleTypeSymbol) {
            errors.add(BindingError.raise("Type `TupleTypeSymbol` is immutable and does not support member reassignment", arrayAssignmentExpression.getSpan()));
        }
        if (array instanceof BoundVariableExpression && ((BoundVariableExpression) array).getVariable().isReadOnly()) {
            errors.add(BindingError.raiseConstReassignmentError(((BoundVariableExpression) array).getVariable(), arrayAssignmentExpression.getArrayAccessExpression().getSpan()));
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

        assignment = typeCheck(boundMemberAccessorExpression.getMember().getType(), assignment, memberAssignmentExpression.getAssignment().getSpan());

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

        TypeSymbol rangeType = range.getType();

        //Type inference
        if (type == UNIT) {
            type = rangeType;
        }

        if (!type.isAssignableFrom(rangeType)) {
            errors.add(BindingError.raiseTypeMismatch(type, rangeType, forExpression.getRangeExpression().getSpan()));
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

        TypeSymbol iteratorType = parseType(forInExpression.getTypeExpression());
        if (iteratorType == ERROR) {
            return new BoundErrorExpression();
        }

        //Type inference
        TypeSymbol iterableType = iterable.getType();
        if (iterableType == ERROR) {
            return new BoundErrorExpression();
        }
        if (iteratorType == UNIT) {
            if (iterableType == STRING) {
                iteratorType = CHAR;
            } else {
                iteratorType = ((ArrayTypeSymbol) iterableType).getType();
            }
        }

        if (!(iterableType == STRING && iteratorType == CHAR)) {
            if (!(iterableType instanceof ArrayTypeSymbol)) {
                errors.add(BindingError.raiseTypeMismatch(new ArrayTypeSymbol(iteratorType), iterableType, forInExpression.getIterable().getSpan()));
                return new BoundErrorExpression();
            }
            if (!iteratorType.isAssignableFrom(((ArrayTypeSymbol) iterableType).getType())) {
                errors.add(BindingError.raiseTypeMismatch(new ArrayTypeSymbol(iteratorType), iterableType, forInExpression.getIterable().getSpan()));
            }
        }
        VariableSymbol variable = buildVariableSymbol(iteratorType, forInExpression.getIdentifier(), null, false, forInExpression);

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
            return UNIT;
        }
        if (typeExpression.getTypeExpression() instanceof TupleTypeExpression) {
            List<TypeSymbol> boundTypes = ((TupleTypeExpression) typeExpression.getTypeExpression()).getTypeExpressions()
                    .stream()
                    .map(DelimitedExpression::getExpression)
                    .map(this::parseType)
                    .collect(Collectors.toList());
            typeSymbol = new TupleTypeSymbol(boundTypes);
        } else if (typeExpression.getTypeExpression() instanceof UnionTypeExpression) {
            List<TypeSymbol> boundTypes = ((UnionTypeExpression) typeExpression.getTypeExpression()).getTypeExpressions()
                    .stream()
                    .map(DelimitedExpression::getExpression)
                    .map(this::parseType)
                    .collect(Collectors.toList());
            typeSymbol = new UnionTypeSymbol(boundTypes);
        } else {
            IdentifierExpression identifier = getTypeIdentifier(typeExpression);

            typeSymbol = getTypeSymbol(identifier);
        }
        if (typeSymbol == null) {
            errors.add(BindingError.raiseUnknownType((String) getTypeIdentifier(typeExpression).getValue(), typeExpression.getSpan()));
            return ERROR;
        }

        //TODO: This doesn't do N-Dimensional arrays yet
        if (typeExpression instanceof ArrayTypeExpression) {
            return new ArrayTypeSymbol(typeSymbol);
        }
        return typeSymbol;
    }

    private TypeSymbol getTypeSymbol(IdentifierExpression identifier) {
        if (identifier == null) {
            return UNIT;
        }
        TypeSymbol typeSymbol;
        switch (identifier.getTokenType()) {
            case UNIT_KEYWORD:
                typeSymbol = UNIT;
                break;
            case CHAR_KEYWORD:
                typeSymbol = CHAR;
                break;
            case INT_KEYWORD:
                typeSymbol = INT;
                break;
            case BOOL_KEYWORD:
                typeSymbol = BOOL;
                break;
            case STRING_KEYWORD:
                typeSymbol = STRING;
                break;
            case REAL_KEYWORD:
                typeSymbol = REAL;
                break;
            case FUNCTION_KEYWORD:
                typeSymbol = FUNCTION;
                break;
            case ANY_KEYWORD:
                typeSymbol = ANY;
                break;
            case FILE_KEYWORD:
                typeSymbol = FILE;
                break;
            default:
                Optional<TypeSymbol> type = currentScope.tryLookupType((String) identifier.getValue()).or(() -> currentScope.tryLookupGenericType((String) identifier.getValue()));
                if (type.isEmpty()) {
                    errors.add(BindingError.raiseUnknownType((String) identifier.getValue(), identifier.getSpan()));
                    typeSymbol = null;
                } else {
                    typeSymbol = type.get();
                }
        }
        return typeSymbol;
    }

    private VariableSymbol buildVariableSymbol(TypeSymbol type, IdentifierExpression identifier, BoundExpression
            guard, boolean readOnly, Expression declaration) {
        return new VariableSymbol((String) identifier.getValue(), type, guard, readOnly, declaration);
    }

    private BoundExpression bindIfExpression(IfExpression ifExpression) {

        BoundExpression condition = bind(ifExpression.getCondition());
        if (!condition.getType().isAssignableFrom(BOOL)) {
            errors.add(BindingError.raiseTypeMismatch(BOOL, condition.getType(), ifExpression.getCondition().getSpan()));
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
                if (right.getType().isAssignableFrom(left.getType())) {
                    left = typeCheck(right.getType(), left, binaryExpression.getLeft().getSpan());
                } else if (left.getType().isAssignableFrom(right.getType())) {
                    right = typeCheck(left.getType(), right, binaryExpression.getRight().getSpan());
                }
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
//            } else if (left.isConstExpression() && left.getType() == STRING && right.isConstExpression() && right.getType() == STRING) {
//
//                if (operator.getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.CONCATENATION) {
//                    return new BoundLiteralExpression(left.getConstValue() + (String) right.getConstValue());
//                }
//                if (operator.getBoundOpType() == BoundBinaryOperator.BoundBinaryOperation.EQUALS) {
//                    return new BoundLiteralExpression(left.getConstValue().equals(right.getConstValue()));
//                }
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
                || identifierExpression.getTokenType() == TokenType.CHAR_LITERAL
                || identifierExpression.getTokenType() == TokenType.UNIT_LITERAL) {
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
        if (variable.isEmpty()) {
            errors.add(BindingError.raiseUnknownIdentifier((String) identifierExpression.getValue(), identifierExpression.getSpan()));
            return new BoundErrorExpression();
        }
        if (variable.get().getDeclaration().getExpressionType() != ExpressionType.VAR_DECLARATION_EXPR) {
            return new BoundVariableExpression(variable.get());
        }
        if (((VariableDeclarationExpression) variable.get().getDeclaration()).getInitialiser() == null) {
            errors.add(BindingError.raiseUninitialisedVariable((String) identifierExpression.getValue(), identifierExpression.getSpan()));
            return new BoundErrorExpression();
        }
        return new BoundVariableExpression(variable.get());
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

        return new BoundLiteralExpression(boundExpression.getType().getName() + "\n");
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

        initialiser = typeCheck(variable.getType(), initialiser, assignmentExpression.getAssignment().getSpan());

        return new BoundAssignmentExpression(variable, variable.getGuard(), initialiser);
    }

    private BoundExpression bindStructDeclarationExpression(StructDeclarationExpression structDeclarationExpression) {

        TypeExpression typeExpressiion = structDeclarationExpression.getTypeDefinition();

        IdentifierExpression typeIdentifier = getTypeIdentifier(typeExpressiion);

        currentScope = new BoundScope(currentScope);

        if (typeExpressiion instanceof GenericTypeExpression) {
            for (IdentifierExpression genericParameter : ((GenericTypeExpression) typeExpressiion).getGenericParameters()) {
                TypeSymbol genericType = new TypeSymbol((String) genericParameter.getValue(), new LinkedHashMap<>());
                currentScope.declareGenericType(genericType.getName(), genericType);
            }
        }

        List<BoundExpression> members = new ArrayList<>();
        for (Expression member : structDeclarationExpression.getMembers()) {
            members.add(bind(member));
        }

        LinkedHashMap<String, VariableSymbol> definedVariables = currentScope.getDefinedVariables();

        currentScope = currentScope.getParentScope();

        TypeSymbol typeSymbol;
        if (typeExpressiion instanceof GenericTypeExpression) {
            List<String> genericParameters = new ArrayList<>();
            for (IdentifierExpression genericParameter : ((GenericTypeExpression) typeExpressiion).getGenericParameters()) {
                genericParameters.add((String) genericParameter.getValue());
            }
            typeSymbol = new ParameterisedTypeSymbol((String) typeIdentifier.getValue(), definedVariables, genericParameters);
        } else {
            typeSymbol = new TypeSymbol((String) typeIdentifier.getValue(), definedVariables);
        }

        try {
            currentScope.declareType(typeSymbol.getName(), typeSymbol);
        } catch (TypeAlreadyDeclaredException tade) {
            errors.add(BindingError.raiseTypeAlreadyDeclared(typeSymbol.getName(), structDeclarationExpression.getTypeDefinition().getSpan()));
        }

        return new BoundStructDeclarationExpression(typeSymbol, members);
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

        currentScope = new BoundScope(currentScope);

        boolean genericFunction = false;
        if (!functionDeclarationExpression.getGenericParameters().isEmpty()) {
            genericFunction = true;
            for (Expression genericParam : functionDeclarationExpression.getGenericParameters()) {
                if (genericParam.getExpressionType() != ExpressionType.TYPE_EXPR) {
                    throw new UnsupportedOperationException("Generic function parameters can only be of type TYPE_EXPR, got `" + genericParam.getExpressionType() + "`");
                }
                IdentifierExpression generic = getTypeIdentifier((TypeExpression) genericParam);
                if (!(genericParam instanceof TypeParameterExpression)) {
                    continue;
                }

                GenericTypeSymbol genericType = new GenericTypeSymbol((String) generic.getValue(), new LinkedHashMap<>());

                try {
                    currentScope.declareType((String) generic.getValue(), genericType);
                } catch (TypeAlreadyDeclaredException tade) {
                    errors.add(BindingError.raiseTypeAlreadyDeclared((String) generic.getValue(), generic.getSpan()));
                }
                List<TypeExpression> constraints = ((TypeParameterExpression) genericParam).getConstraints();
                for (TypeExpression constraint : constraints) {
                    TypeSymbol constraintType = getTypeSymbol(getTypeIdentifier(constraint));

                    if (!(constraintType instanceof InterfaceTypeSymbol)) {
                        errors.add(BindingError.raise("Generic type parameters can only be constrained by interface types", constraint.getSpan()));
                        continue;
                    }
                    List<BoundFunctionSignatureExpression> signatures = ((InterfaceTypeSymbol) constraintType).getSignatures();
                    for (BoundFunctionSignatureExpression functionSignatureExpression : signatures) {

                        List<BoundFunctionParameterExpression> functionParameterExpressions = new ArrayList<>();
                        VariableSymbol self = new VariableSymbol("self", genericType, null, true, null);
                        functionParameterExpressions.add(new BoundFunctionParameterExpression(false, self, null));
                        functionParameterExpressions.addAll(functionSignatureExpression.getFunctionParameterExpressions());

                        FunctionSymbol interfaceFunction = new FunctionSymbol(functionSignatureExpression.getIdentifier(), functionSignatureExpression.getReturnType(), functionParameterExpressions, null);

                        List<String> argumentIdentifiers = functionParameterExpressions.stream()
                                .map(BoundFunctionParameterExpression::getType)
                                .map(Object::toString)
                                .collect(Collectors.toList());
                        try {
                            currentScope.declareFunction(buildSignature(functionSignatureExpression.getIdentifier(), argumentIdentifiers), interfaceFunction);
                        } catch (FunctionAlreadyDeclaredException fade) {
                            errors.add(BindingError.raiseFunctionAlreadyDeclared(functionSignatureExpression.getIdentifier(), constraint.getSpan()));
                        }
                    }
                }
            }
        }
        TypeSymbol type = parseType(functionDeclarationExpression.getTypeExpression());

        //Declare the arguments within the function's scope
        List<BoundFunctionParameterExpression> arguments = new ArrayList<>();
        for (FunctionParameterExpression argumentExpression : functionDeclarationExpression.getArguments()) {
            BoundFunctionParameterExpression boundArgument = bindFunctionArgumentExpression(argumentExpression);
            arguments.add(boundArgument);
        }

        FunctionSymbol functionSymbol = new FunctionSymbol((String) identifier.getValue(), type, arguments, null);
        List<String> argumentIdentifiers = functionDeclarationExpression.getArguments().stream()
                .map(FunctionParameterExpression::getTypeExpression)
                .map(this::getTypeIdentifier)
                .map(IdentifierExpression::getValue)
                .map(Object::toString)
                .collect(Collectors.toList());
        boolean deferredFunction = genericFunction || functionSymbol.getArguments().stream()
                .anyMatch(arg -> arg.getType() instanceof InterfaceTypeSymbol);

        if (deferredFunction) {
            currentScope.getParentScope().declareInterfaceFunction((String) functionDeclarationExpression.getIdentifier().getValue(), functionSymbol);

            interfaceBodies.put(functionSymbol, functionDeclarationExpression.getBody());

            //quickly check the body
            currentScope = new BoundScope(currentScope);
            bindBlockExpression(functionDeclarationExpression.getBody());
            currentScope = currentScope.getParentScope();

            currentScope = currentScope.getParentScope();
            return new BoundNoOpExpression();
        }

        try {
            currentScope.getParentScope().declareFunction(buildSignature((String) functionDeclarationExpression.getIdentifier().getValue(), argumentIdentifiers), functionSymbol);
        } catch (FunctionAlreadyDeclaredException fade) {
            List<SyntaxNode> children = new ArrayList<>();
            children.add(functionDeclarationExpression.getFnKeyword());
            children.add(functionDeclarationExpression.getIdentifier());
            children.add(functionDeclarationExpression.getOpenParen());
            children.addAll(functionDeclarationExpression.getArguments());
            children.add(functionDeclarationExpression.getCloseParen());
            errors.add(BindingError.raiseFunctionAlreadyDeclared(functionSymbol.getSignature(), Expression.getSpan(children)));
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

    private String buildSignature(String name, List<String> typeIdentifiers) {
        StringBuilder sb = new StringBuilder();

        sb.append(name);
//        if (!functionDeclarationExpression.getGenericParameters().isEmpty()) {
//            sb.append("<");
//            for (Expression genericParameter : functionDeclarationExpression.getGenericParameters()) {
//                if (genericParameter instanceof IdentifierExpression) {
//                    sb.append(((IdentifierExpression) genericParameter).getValue());
//                }
//            }
//            sb.append(">");
//        }
        sb.append("(");
        sb.append(String.join(", ", typeIdentifiers));
        sb.append(")");
        return sb.toString();
    }

    private void typeCheckMainFunction(BoundFunctionDeclarationExpression boundMainFunction, FunctionDeclarationExpression mainFunction) {

        if (boundMainFunction.getFunctionSymbol().getType() != UNIT) {
            errors.add(BindingError.raiseTypeMismatch(UNIT, boundMainFunction.getFunctionSymbol().getType(), mainFunction.getTypeExpression().getSpan()));
        }
        List<BoundFunctionParameterExpression> arguments = boundMainFunction.getArguments();
        if (!arguments.isEmpty()) {
            if (arguments.size() == 1) {
                BoundFunctionParameterExpression argumentExpression = arguments.get(0);
                if (!argumentExpression.getType().equals(new ArrayTypeSymbol(STRING))) {
                    errors.add(BindingError.raiseTypeMismatch(new ArrayTypeSymbol(STRING), argumentExpression.getType(), mainFunction.getArguments().get(0).getSpan()));
                }
            } else {
                BoundFunctionParameterExpression argumentExpression = arguments.get(0);
                if (!argumentExpression.getType().equals(new ArrayTypeSymbol(STRING))) {
                    errors.add(BindingError.raiseTypeMismatch(new ArrayTypeSymbol(STRING), argumentExpression.getType(), mainFunction.getArguments().get(0).getTypeExpression().getSpan()));
                }
                for (int i = 1; i < arguments.size(); i++) {
                    errors.add(BindingError.raiseTypeMismatch(UNIT, boundMainFunction.getArguments().get(i).getType(), mainFunction.getArguments().get(i).getTypeExpression().getSpan()));
                }
            }
        }
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
            if (guard.getType() != BOOL) {
                errors.add(BindingError.raiseTypeMismatch(BOOL, guard.getType(), argumentExpression.getGuard().getSpan()));
            }
        }

        VariableSymbol argument = buildVariableSymbol(type, identifier, guard, argumentExpression.getConstKeyword() != null, argumentExpression);

        currentScope.reassignVariable((String) identifier.getValue(), argument);

        return new BoundFunctionParameterExpression(reference, argument, guard);
    }

    private BoundExpression bindFunctionCallExpression(FunctionCallExpression functionCallExpression) {

        IdentifierExpression identifier = functionCallExpression.getIdentifier();

        List<BoundExpression> boundArguments = new ArrayList<>();
        List<FunctionCallArgumentExpression> argExpressions = functionCallExpression.getArguments();

        for (FunctionCallArgumentExpression argExpression : argExpressions) {
            Expression functionCallExpressionArgument = argExpression.getExpression();
            if (functionCallExpressionArgument.getExpressionType() == ExpressionType.STRUCT_LITERAL_EXPRESSION) {
                List<BoundExpression> boundMembers = new ArrayList<>();
                StructLiteralExpression structLiteralExpression = (StructLiteralExpression) functionCallExpressionArgument;
                for (Expression member : structLiteralExpression.getMembers()) {
                    boundMembers.add(bind(member));
                }
                boundArguments.add(new BoundStructLiteralExpression(getTypeSymbol(getTypeIdentifier(structLiteralExpression.getTypeExpression())), boundMembers));
            } else {
                BoundExpression boundArgument = bind(functionCallExpressionArgument);
                boundArguments.add(boundArgument);
            }
        }

        List<String> argumentTypeIdentifiers = boundArguments.stream()
                .map(BoundExpression::getType)
                .map(TypeSymbol::toString)
                .collect(Collectors.toList());
        String signature = buildSignature((String) functionCallExpression.getIdentifier().getValue(), argumentTypeIdentifiers);

        Optional<FunctionSymbol> scopedFunction = currentScope.tryLookupFunction(signature);

        if (scopedFunction.isEmpty()) {

            Set<FunctionSymbol> potentialFunctions = currentScope.tryLookupInterfaceFunctions(identifier.getValue().toString());
            if (potentialFunctions.isEmpty()) {
                errors.add(BindingError.raiseUnknownFunction((String) identifier.getValue(), boundArguments, functionCallExpression.getSpan()));
                return new BoundErrorExpression();
            }

            List<FunctionSymbol> compatibleFunctions = potentialFunctions.stream()
                    .filter(func -> isCompatible(boundArguments, func))
                    .collect(Collectors.toList());
            if (compatibleFunctions.isEmpty()) {
                errors.add(BindingError.raiseUnknownFunction((String) identifier.getValue(), boundArguments, functionCallExpression.getSpan()));
                return new BoundErrorExpression();
            }
            if (compatibleFunctions.size() > 1) {
                throw new IllegalStateException("More than one function with matching signature found");
            }

            //Build function for the provided type
            FunctionSymbol implementation = buildImplementationFunction(boundArguments, compatibleFunctions.get(0));

            scopedFunction = Optional.of(implementation);
        }

        FunctionSymbol function = scopedFunction.get();

        if (function.getArguments().size() != functionCallExpression.getArguments().size()) {
            errors.add(BindingError.raiseUnknownFunction((String) identifier.getValue(), boundArguments, functionCallExpression.getSpan()));
            return new BoundFunctionCallExpression(function, boundArguments);
        }

        List<BoundFunctionParameterExpression> parameters = function.getArguments();
        for (int i = 0; i < argExpressions.size(); i++) {
            BoundExpression boundArgument = boundArguments.get(i);
            boundArgument = typeCheck(parameters.get(i).getType(), boundArgument, argExpressions.get(i).getSpan());

            boolean passByRef = argExpressions.get(i).getRefKeyword() != null;
            if (parameters.get(i).isReference()) {
                if (boundArgument instanceof BoundLiteralExpression || boundArgument instanceof BoundStructLiteralExpression) {
                    errors.add(BindingError.raise("Literals cannot be passed by reference:", argExpressions.get(i).getSpan()));
                }
                if (!passByRef) {
                    errors.add(BindingError.raise("Argument " + i + " of `" + function.getSignature() + "` must be passed with the `ref` keyword", argExpressions.get(i).getSpan()));
                }
            } else if (passByRef) {
                errors.add(BindingError.raise("Argument " + i + " of `" + function.getSignature() + "` is not passed with the `ref` keyword", argExpressions.get(i).getSpan()));
            }
        }

        return new BoundFunctionCallExpression(function, boundArguments);
    }

    private FunctionSymbol buildImplementationFunction(List<BoundExpression> boundArguments, FunctionSymbol interfaceFunction) {

        List<BoundFunctionParameterExpression> implFunctionParams = new ArrayList<>();
        for (int i = 0; i < boundArguments.size(); i++) {
            BoundExpression boundArg = boundArguments.get(i);
            BoundFunctionParameterExpression funcArg = interfaceFunction.getArguments().get(i);

            VariableSymbol implVariable = (funcArg.getType() instanceof InterfaceTypeSymbol || funcArg.getType() instanceof GenericTypeSymbol)
                    ? new VariableSymbol(funcArg.getArgument().getName(), boundArg.getType(), funcArg.getArgument().getGuard(), funcArg.getArgument().isReadOnly(), funcArg.getArgument().getDeclaration())
                    : funcArg.getArgument();

            implFunctionParams.add(new BoundFunctionParameterExpression(funcArg.isReference(), implVariable, funcArg.getGuard()));
        }
        FunctionSymbol impl = new FunctionSymbol(interfaceFunction.getName(), interfaceFunction.getType(), implFunctionParams, interfaceFunction.getGuard());

        //Build body
        BlockExpression body = interfaceBodies.get(interfaceFunction);
        if (body == null) {
            throw new IllegalStateException("No such body for interface function `" + interfaceFunction.getSignature() + "`");
        }

        BoundScope savedScope = currentScope;

        //Need to declare this in the top scope
        while (currentScope.getParentScope() != null) {
            currentScope = currentScope.getParentScope();
        }

        //Declare the arguments within the function's scope
        currentScope = new BoundScope(currentScope);
        for (BoundFunctionParameterExpression implFunctionParam : implFunctionParams) {
            currentScope.declareVariable(implFunctionParam.getArgument().getName(), implFunctionParam.getArgument());
        }

        List<String> argumentIdentifiers = implFunctionParams.stream()
                .map(BoundFunctionParameterExpression::getType)
                .map(TypeSymbol::toString)
                .collect(Collectors.toList());
        try {
            currentScope.declareFunction(buildSignature(impl.getName(), argumentIdentifiers), impl);
        } catch (FunctionAlreadyDeclaredException fade) {
            return impl;
        }

        BoundBlockExpression boundBody = bindBlockExpression(body);

        //TODO: No analysis done on the interface method
        //errors.addAll(FunctionAnalyser.analyzeBody(impl, boundBody.getExpressions(), body.getExpressions(), functionDeclarationExpression));

        currentScope = savedScope;

        boundExpressions.add(new BoundFunctionDeclarationExpression(impl, implFunctionParams, boundBody));

        return impl;
    }

    private boolean isCompatible(List<BoundExpression> arguments, FunctionSymbol func) {
        if (func.getArguments().size() != arguments.size()) {
            return false;
        }

        for (int i = 0; i < arguments.size(); i++) {
            TypeSymbol expectedType = func.getArguments().get(i).getType();
            TypeSymbol actualType = arguments.get(i).getType();

            if (expectedType instanceof InterfaceTypeSymbol) {
                continue;
            }
            if (expectedType instanceof GenericTypeSymbol) {
                continue;
            }
            if (!expectedType.isAssignableFrom(actualType)) {
                return false;
            }
        }
        return true;
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
            if (type == null) {
                return new BoundErrorExpression();
            }
            currentScope.declareVariable((String) identifier.getValue(), new VariableSymbol((String) identifier.getValue(), type, null, false, variableDeclarationExpression));
        } catch (VariableAlreadyDeclaredException vade) {
            VariableSymbol alreadyDeclaredVariable = currentScope.tryLookupVariable((String) identifier.getValue()).get();
            errors.add(BindingError.raiseVariableAlreadyDeclared(alreadyDeclaredVariable, identifier.getSpan(), alreadyDeclaredVariable.getDeclaration().getSpan()));
        }

        BoundExpression guard = null;
        if (variableDeclarationExpression.getGuard() != null) {
            guard = bind(variableDeclarationExpression.getGuard());
            assert guard.getType().isAssignableFrom(BOOL);
        }

        TypeSymbol type = parseType(variableDeclarationExpression.getTypeExpression());
        if (type == null) {
            if (variableDeclarationExpression.getTypeExpression().getTypeExpression() instanceof TupleTypeExpression) {
                throw new UnsupportedOperationException("TupleTypeSymbol types are not yet supported in variable declarations");
            }
            IdentifierExpression typeExpression = (IdentifierExpression) variableDeclarationExpression.getTypeExpression().getTypeExpression();
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

        VariableSymbol variable = buildVariableSymbol(type, identifier, guard, readOnly, variableDeclarationExpression);

        currentScope.reassignVariable((String) identifier.getValue(), variable);
        return new BoundVariableDeclarationExpression(variable, guard, initialiser, readOnly);
    }

    private BoundExpression bindWhileExpression(WhileExpression whileExpression) {

        BoundExpression condition = bind(whileExpression.getCondition());
        if (!condition.getType().isAssignableFrom(BOOL)) {
            errors.add(BindingError.raiseTypeMismatch(BOOL, condition.getType(), whileExpression.getCondition().getSpan()));
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

    private IdentifierExpression getTypeIdentifier(TypeExpression typeExpression) {

        if (typeExpression == null) {
            return null;
        }
        if (typeExpression.getTypeExpression() instanceof IdentifierExpression) {
            return (IdentifierExpression) typeExpression.getTypeExpression();
        }
        if (typeExpression.getTypeExpression() instanceof ArrayTypeExpression) {
            return getTypeIdentifier((ArrayTypeExpression) typeExpression.getTypeExpression());
        }
        if (typeExpression.getTypeExpression() instanceof GenericTypeExpression) {
            return getTypeIdentifier((GenericTypeExpression) typeExpression.getTypeExpression());
        }
        if (typeExpression.getTypeExpression() instanceof ErasedParameterisedTypeExpression) {
            return getTypeIdentifier((ErasedParameterisedTypeExpression) typeExpression.getTypeExpression());
        }
        throw new UnsupportedOperationException("Unhandled type expression type `" + typeExpression.getTypeExpression().getClass().getSimpleName() + "`");
    }

    private BoundExpression typeCheck(TypeSymbol expected, BoundExpression expression, TextSpan location) {

        TypeSymbol actual = expression.getType();

        if (expected.equals(actual)) {
            return expression;
        }
        if (expected.isAssignableFrom(actual)) {
            return new BoundCastExpression(expression, expected);
        }

        errors.add(BindingError.raiseTypeMismatch(expected, actual, location));
        return new BoundErrorExpression();
    }
}
