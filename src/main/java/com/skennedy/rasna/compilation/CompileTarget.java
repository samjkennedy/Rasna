package com.skennedy.rasna.compilation;

public enum CompileTarget {
    JVM("jvm"),
    LLVM("llvm");

    private final String id;

    CompileTarget(String id) {
        this.id = id;
    }

    public static CompileTarget fromId(String id) {
        for (CompileTarget compileTarget : CompileTarget.values()) {
            if (compileTarget.id.equals(id)) {
                return compileTarget;
            }
        }
        throw new IllegalArgumentException("Unknown target: " + id);
    }
}
