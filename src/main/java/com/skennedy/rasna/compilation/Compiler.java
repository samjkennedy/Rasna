package com.skennedy.rasna.compilation;

import com.skennedy.rasna.typebinding.BoundProgram;

import java.io.IOException;

public interface Compiler {

    void compile(BoundProgram program, String outputFileName) throws IOException;
}
