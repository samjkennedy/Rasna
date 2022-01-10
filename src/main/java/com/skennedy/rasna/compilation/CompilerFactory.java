package com.skennedy.rasna.compilation;

public class CompilerFactory {

    public static Compiler create(CompileTarget target) {
        switch (target) {
            case JVM:
                return new JavaBytecodeCompiler();
            case LLVM:
                throw new UnsupportedOperationException("Compiling for " + target + " is not yet supported");
            default:
                throw new IllegalArgumentException("Unknown target: " + target);
        }
    }
}
