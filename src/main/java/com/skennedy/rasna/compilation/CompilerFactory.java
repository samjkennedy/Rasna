package com.skennedy.rasna.compilation;

import com.skennedy.rasna.compilation.llvm.LLVMCompiler;

public class CompilerFactory {

    public static Compiler create(CompileTarget target) {
        switch (target) {
            case JVM:
                return new JavaBytecodeCompiler();
            case LLVM:
                return new LLVMCompiler();
            default:
                throw new IllegalArgumentException("Unknown target: " + target);
        }
    }
}
