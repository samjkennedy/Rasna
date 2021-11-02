package com.skennedy.lazuli.compilation;

import com.skennedy.lazuli.typebinding.BoundProgram;

import java.io.IOException;

public interface Compiler {

    void compile(BoundProgram program, String outputFileName) throws IOException;
}
