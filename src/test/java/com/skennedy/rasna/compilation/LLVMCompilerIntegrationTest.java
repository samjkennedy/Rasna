package com.skennedy.rasna.compilation;

import com.skennedy.rasna.compilation.llvm.LLVMCompiler;
import com.skennedy.rasna.diagnostics.BindingError;
import com.skennedy.rasna.diagnostics.Error;
import com.skennedy.rasna.lowering.LLVMLowerer;
import com.skennedy.rasna.parsing.Parser;
import com.skennedy.rasna.parsing.Program;
import com.skennedy.rasna.typebinding.Binder;
import com.skennedy.rasna.typebinding.BoundProgram;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LLVMCompilerIntegrationTest extends CompilerBaseIntegrationTest {

    @ParameterizedTest
    @MethodSource("getFilesToTest")
    void runFile_producesCorrectOutput(String filename) throws IOException {

        PrintStream console = System.out;
        //Set up output stream
        File outputFile = new File("src/test/resources/results/compilation/llvm/" + filename.split("\\.")[0] + "_result.txt");
        outputFile.createNewFile();
        PrintStream out = new PrintStream(outputFile);
        // Store current System.out before assigning a new value
        //Set output to write to file
        System.setOut(out);

        String code = read("tests", filename);

        Parser parser = new Parser();
        Program program = parser.parse(Path.of(getFullPath("tests", filename)).toAbsolutePath(), code);
        if (program.hasErrors()) {
            for (Error error : program.getErrors()) {
                highlightError(error, code.lines().collect(Collectors.toList()));
            }
        }

        Binder binder = new Binder();
        BoundProgram boundProgram = binder.bind(program);

        LLVMLowerer lowerer = new LLVMLowerer();
        boundProgram = lowerer.rewrite(boundProgram);

        if (boundProgram.hasErrors()) {
            for (BindingError error : boundProgram.getErrors()) {
                highlightBindingError(error, code.lines().collect(Collectors.toList()));
            }
        } else {

            LLVMCompiler compiler = new LLVMCompiler();
            compiler.compile(boundProgram, filename.split("\\.")[0]);

            Process process = Runtime.getRuntime().exec(filename.split("\\.")[0] + ".exe");
            InputStream inputStream = process.getInputStream();
            char c = (char) inputStream.read();
            while (c != '\uFFFF') {
                System.out.print(c);
                c = (char) inputStream.read();
            }
            InputStream errorStream = process.getErrorStream();
            c = (char) errorStream.read();
            while (c != '\uFFFF') {
                System.out.print(c);
                c = (char) errorStream.read();
            }
        }
        //Reset console
        System.setOut(console);

        if (!boundProgram.hasErrors()) {
            File classFile = new File(filename.split("\\.")[0] + ".ll");
            assertTrue(classFile.delete(), "Could not delete ll file");
            File bytecodeFile = new File(filename.split("\\.")[0] + ".exe");
            assertTrue(bytecodeFile.delete(), "Could not delete exe file");
        }

        String expectedResult = read("results/expected", filename.split("\\.")[0] + "_result.txt").trim();
        String actualResult = read("results/compilation", "llvm/" + filename.split("\\.")[0] + "_result.txt").trim();

        assertEquals(expectedResult, actualResult);
    }
}
