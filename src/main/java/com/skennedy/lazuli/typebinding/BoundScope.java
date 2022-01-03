package com.skennedy.lazuli.typebinding;

import com.skennedy.lazuli.exceptions.FunctionAlreadyDeclaredException;
import com.skennedy.lazuli.exceptions.TypeAlreadyDeclaredException;
import com.skennedy.lazuli.exceptions.UndefinedVariableException;
import com.skennedy.lazuli.exceptions.VariableAlreadyDeclaredException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BoundScope {

    private final BoundScope parentScope;
    private final Map<String, VariableSymbol> definedVariables;
    private final Map<String, FunctionSymbol> definedFunctions;
    private final Map<String, TypeSymbol> definedTypes;
    private final Map<String, BoundScope> namespaces;

    public BoundScope(BoundScope parentScope) {
        this.parentScope = parentScope;
        this.definedVariables = new HashMap<>();
        this.definedFunctions = new HashMap<>();
        this.definedTypes = new HashMap<>();
        this.namespaces = new HashMap<>();
    }

    public static BoundScope merge(BoundScope primary, BoundScope secondary) {
        BoundScope merged = new BoundScope(primary);

        secondary.definedVariables.forEach(merged::declareVariable);
        secondary.definedFunctions.forEach(merged::declareFunction);
        secondary.definedTypes.forEach(merged::declareType);
        secondary.namespaces.forEach(merged::declareNamespace);

        return merged;
    }

    public BoundScope getParentScope() {
        return parentScope;
    }

    public Optional<VariableSymbol> tryLookupVariable(String name) {

        if (definedVariables.containsKey(name)) {
            return Optional.of(definedVariables.get(name));
        }
        if (parentScope != null) {
            return parentScope.tryLookupVariable(name);
        }
        return Optional.empty();
    }

    public Optional<FunctionSymbol> tryLookupFunction(String name) {

        if (definedFunctions.containsKey(name)) {
            return Optional.of(definedFunctions.get(name));
        }
        if (parentScope != null) {
            return parentScope.tryLookupFunction(name);
        }
        return Optional.empty();
    }

    public Optional<TypeSymbol> tryLookupType(String name) {

        if (definedTypes.containsKey(name)) {
            return Optional.of(definedTypes.get(name));
        }
        if (parentScope != null) {
            return parentScope.tryLookupType(name);
        }
        return Optional.empty();
    }

    public Optional<BoundScope> tryLookupNamespace(String name) {

        if (namespaces.containsKey(name)) {
            return Optional.of(namespaces.get(name));
        }
        if (parentScope != null) {
            return parentScope.tryLookupNamespace(name);
        }
        return Optional.empty();
    }

    public void declareVariable(String name, VariableSymbol variable) {
        if (tryLookupVariable(name).isPresent()) {
            throw new VariableAlreadyDeclaredException(name);
        }
        definedVariables.put(name, variable);
    }

    public void reassignVariable(String name, VariableSymbol variable) {
        if (!definedVariables.containsKey(name)) {
            if (parentScope == null) {
                throw new UndefinedVariableException(name);
            }
            parentScope.reassignVariable(name, variable);
        }
        definedVariables.replace(name, variable);
    }

    public void declareFunction(String name, FunctionSymbol function) {
        if (tryLookupFunction(name).isPresent()) {
            throw new FunctionAlreadyDeclaredException(name);
        }
        definedFunctions.put(name, function);
    }

    public void declareType(String name, TypeSymbol type) {
        if (tryLookupType(name).isPresent()) {
            throw new TypeAlreadyDeclaredException(name);
        }
        definedTypes.put(name, type);
    }

    public void declareNamespace(String name, BoundScope scope) {
        if (tryLookupNamespace(name).isPresent()) {
            throw new TypeAlreadyDeclaredException(name);
        }
        namespaces.put(name, scope);
    }

    public Map<String, FunctionSymbol> getDefinedFunctions() {
        return definedFunctions;
    }

    public Map<String, VariableSymbol> getDefinedVariables() {
        return definedVariables;
    }
}
