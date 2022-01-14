package com.skennedy.rasna.lowering;

import com.skennedy.rasna.compilation.CompileTarget;

/**
 * Different IRs require different ASTs
 *
 * This returns the appropriate lowerer for the given compile target
 */
public class LowererFactory {

    private LowererFactory() {
        throw new IllegalStateException("Should not be instantiated");
    }

    public static BoundProgramRewriter getLowerer(CompileTarget target) {
        switch (target) {
            case JVM:
                return new JVMLowerer();
            case LLVM:
                return new LLVMLowerer();
            default:
                throw new IllegalStateException("Unknown compile target: " + target);
        }
    }
}
