package com.skennedy.rasna.compilation.llvm;

import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMContextRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

public class Scope {

    private final LLVMBuilderRef builder;
    private final LLVMContextRef context;
    private final LLVMValueRef function;
    private final Scope parentScope;

    public Scope(LLVMBuilderRef builder, LLVMContextRef context, LLVMValueRef function) {
        this.parentScope = null;
        this.builder = builder;
        this.context = context;
        this.function = function;
    }

    public Scope(Scope parentScope) {
        this.parentScope = parentScope;
        this.builder = parentScope.builder;
        this.context = parentScope.context;
        this.function = parentScope.function;
    }

    public Scope getParentScope() {
        return parentScope;
    }

    public LLVMBuilderRef getBuilder() {
        return builder;
    }

    public LLVMContextRef getContext() {
        return context;
    }

    public LLVMValueRef getFunction() {
        return function;
    }
}
