package com.skennedy.rasna.typebinding;

import com.google.common.collect.LinkedHashMultimap;
import com.skennedy.rasna.exceptions.FunctionAlreadyDeclaredException;
import com.skennedy.rasna.exceptions.TypeAlreadyDeclaredException;
import com.skennedy.rasna.exceptions.UndefinedVariableException;
import com.skennedy.rasna.exceptions.VariableAlreadyDeclaredException;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BoundScope {

    private final BoundScope parentScope;
    private final LinkedHashMap<String, VariableSymbol> definedVariables;
    private final LinkedHashMap<String, FunctionSymbol> definedFunctions;
    private final LinkedHashMultimap<String, FunctionSymbol> definedInterfaceFunctions;
    private final Map<String, TypeSymbol> definedTypes;
    private final Map<String, TypeSymbol> definedGenericTypes;
    private final Map<TypeSymbol, TypeSymbol> boundGenericTypes;
    private final Map<String, BoundScope> namespaces;

    public BoundScope(BoundScope parentScope) {
        this.parentScope = parentScope;
        this.definedVariables = new LinkedHashMap<>();
        this.definedFunctions = new LinkedHashMap<>();
        this.definedInterfaceFunctions = LinkedHashMultimap.create();
        this.definedTypes = new HashMap<>();
        this.definedGenericTypes = new HashMap<>();
        this.boundGenericTypes = new HashMap<>();
        this.namespaces = new HashMap<>();
    }

    public static BoundScope merge(BoundScope primary, BoundScope secondary) {
        BoundScope merged = new BoundScope(primary);

        secondary.definedVariables.forEach(merged::declareVariable);
        secondary.definedFunctions.forEach(merged::declareFunction);
        secondary.definedInterfaceFunctions.forEach(merged::declareInterfaceFunction);
        secondary.definedTypes.forEach(merged::declareType);
        secondary.definedGenericTypes.forEach(merged::declareGenericType);
        secondary.boundGenericTypes.forEach(merged::bindGenericType);
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

    public Optional<FunctionSymbol> tryLookupFunction(String signature) {

        if (definedFunctions.containsKey(signature)) {
            return Optional.of(definedFunctions.get(signature));
        }
        if (parentScope != null) {
            return parentScope.tryLookupFunction(signature);
        }
        return Optional.empty();
    }

    public Set<FunctionSymbol> tryLookupInterfaceFunctions(String name) {

        if (definedInterfaceFunctions.containsKey(name)) {
            return definedInterfaceFunctions.get(name);
        }
        if (parentScope != null) {
            return parentScope.tryLookupInterfaceFunctions(name);
        }
        return Collections.emptySet();
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

    public Optional<TypeSymbol> tryLookupGenericType(String name) {

        if (definedGenericTypes.containsKey(name)) {
            return Optional.of(definedGenericTypes.get(name));
        }
        if (parentScope != null) {
            return parentScope.tryLookupGenericType(name);
        }
        return Optional.empty();
    }

    public Optional<TypeSymbol> tryLookupBinding(TypeSymbol genericType) {

        if (boundGenericTypes.containsKey(genericType)) {
            return Optional.of(boundGenericTypes.get(genericType));
        }
        if (parentScope != null) {
            return parentScope.tryLookupBinding(genericType);
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

    public void declareFunction(String signature, FunctionSymbol function) {
        if (tryLookupFunction(signature).isPresent()) {
            throw new FunctionAlreadyDeclaredException(signature);
        }
        definedFunctions.put(signature, function);
    }

    public void declareInterfaceFunction(String name, FunctionSymbol functionSymbol) {
        definedInterfaceFunctions.put(name, functionSymbol);
    }

    public void declareType(String name, TypeSymbol type) {
        if (tryLookupType(name).isPresent()) {
            throw new TypeAlreadyDeclaredException(name);
        }
        definedTypes.put(name, type);
    }

    public void declareGenericType(String name, TypeSymbol genericType) {
        if (tryLookupType(name).isPresent()) {
            throw new TypeAlreadyDeclaredException(name);
        }
        definedTypes.put(name, genericType);
        definedGenericTypes.put(name, genericType);
    }


    public void bindGenericType(TypeSymbol genericType, TypeSymbol concreteType) {
        if (tryLookupBinding(genericType).isPresent()) {
            throw new TypeAlreadyDeclaredException(genericType.getName());
        }
        boundGenericTypes.put(genericType, concreteType);
    }

    public void declareNamespace(String name, BoundScope scope) {
        if (tryLookupNamespace(name).isPresent()) {
            replaceNamespace(name, scope);
        }
        namespaces.put(name, scope);
    }

    private void replaceNamespace(String name, BoundScope scope) {
        if (namespaces.containsKey(name)) {
            namespaces.replace(name, scope);
        }
        if (parentScope != null) {
            parentScope.replaceNamespace(name, scope);
        }
    }

    public LinkedHashMap<String, FunctionSymbol> getDefinedFunctions() {
        return definedFunctions;
    }

    public LinkedHashMap<String, VariableSymbol> getDefinedVariables() {
        return definedVariables;
    }
}
