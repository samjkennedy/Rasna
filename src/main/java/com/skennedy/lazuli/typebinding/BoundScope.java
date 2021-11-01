package com.skennedy.lazuli.typebinding;

import com.skennedy.lazuli.exceptions.FunctionAlreadyDeclaredException;
import com.skennedy.lazuli.exceptions.UndefinedVariableException;
import com.skennedy.lazuli.exceptions.VariableAlreadyDeclaredException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BoundScope {

    private final BoundScope parentScope;
    private final Map<String, VariableSymbol> definedVariables;
    private final Map<String, FunctionSymbol> definedFunctions;

    public BoundScope(BoundScope parentScope) {
        this.parentScope = parentScope;
        this.definedVariables = new HashMap<>();
        this.definedFunctions = new HashMap<>();
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
}
