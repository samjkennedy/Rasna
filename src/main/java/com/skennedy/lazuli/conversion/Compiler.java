package com.skennedy.lazuli.conversion;

import com.skennedy.lazuli.typebinding.BoundProgram;

import java.io.IOException;

public interface Compiler {

    void convert(BoundProgram program, String outputFileName) throws IOException;
}
