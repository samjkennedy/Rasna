package com.skennedy.rasna.compilation.llvm;

import com.skennedy.rasna.exceptions.FunctionAlreadyDeclaredException;
import com.skennedy.rasna.exceptions.TypeAlreadyDeclaredException;
import com.skennedy.rasna.exceptions.VariableAlreadyDeclaredException;
import com.skennedy.rasna.typebinding.EnumTypeSymbol;
import com.skennedy.rasna.typebinding.FunctionSymbol;
import com.skennedy.rasna.typebinding.TypeSymbol;
import com.skennedy.rasna.typebinding.VariableSymbol;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class Scope {

    private final Scope parentScope;

    private Map<VariableSymbol, LLVMValueRef> definedVariables;
    private Map<VariableSymbol, LLVMValueRef> definedPointers;
    private Map<FunctionSymbol, LLVMValueRef> definedFunctions;
    private Map<TypeSymbol, LLVMTypeRef> definedTypes;

    Scope(Scope parentScope) {
        this.parentScope = parentScope;

        definedVariables = new HashMap<>();
        definedPointers = new HashMap<>();
        definedFunctions = new HashMap<>();
        definedTypes = new HashMap<>();
    }

    Scope getParentScope() {
        return parentScope;
    }

    Optional<LLVMValueRef> tryLookupVariable(VariableSymbol variable) {

        if (definedVariables.containsKey(variable)) {
            return Optional.of(definedVariables.get(variable));
        }
        if (parentScope != null) {
            return parentScope.tryLookupVariable(variable);
        }
        return Optional.empty();
    }

    void declareVariable(VariableSymbol variable, LLVMValueRef value) {
        if (tryLookupVariable(variable).isPresent()) {
            throw new VariableAlreadyDeclaredException(variable.getName());
        }
        definedVariables.put(variable, value);
    }

    Optional<LLVMValueRef> tryLookupPointer(VariableSymbol variable) {

        if (definedPointers.containsKey(variable)) {
            return Optional.of(definedPointers.get(variable));
        }
        if (parentScope != null) {
            return parentScope.tryLookupPointer(variable);
        }
        return Optional.empty();
    }

    void declarePointer(VariableSymbol variable, LLVMValueRef ptr) {
        if (tryLookupPointer(variable).isPresent()) {
            throw new VariableAlreadyDeclaredException(variable.getName());
        }
        definedPointers.put(variable, ptr);
    }

    Optional<LLVMValueRef> tryLookupFunction(FunctionSymbol function) {

        if (definedFunctions.containsKey(function)) {
            return Optional.of(definedFunctions.get(function));
        }
        if (parentScope != null) {
            return parentScope.tryLookupFunction(function);
        }
        return Optional.empty();
    }

    void declareFunction(FunctionSymbol function, LLVMValueRef ref) {
        if (tryLookupFunction(function).isPresent()) {
            throw new FunctionAlreadyDeclaredException(function.getName());
        }
        definedFunctions.put(function, ref);
    }

    Optional<LLVMTypeRef> tryLookupType(TypeSymbol type) {

        if (definedTypes.containsKey(type)) {
            return Optional.of(definedTypes.get(type));
        }
        if (parentScope != null) {
            return parentScope.tryLookupType(type);
        }
        return Optional.empty();
    }

    void declareType(TypeSymbol type, LLVMTypeRef ref) {
        if (tryLookupType(type).isPresent()) {
            throw new TypeAlreadyDeclaredException(type.getName());
        }
        definedTypes.put(type, ref);
    }
}
