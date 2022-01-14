package com.skennedy.rasna.compilation;

import com.skennedy.rasna.diagnostics.BindingError;
import com.skennedy.rasna.diagnostics.Error;
import com.skennedy.rasna.lowering.Lowerer;
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

class JavaBytecodeCompilerIntegrationTest extends CompilerBaseIntegrationTest {

    @ParameterizedTest
    @MethodSource("getFilesToTest")
    void runFile_producesCorrectOutput(String filename) throws IOException {

        PrintStream console = System.out;
        //Set up output stream
        File outputFile = new File("src/test/resources/results/compilation/jvm/" + filename.split("\\.")[0] + "_result.txt");
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

        Lowerer lowerer = new Lowerer();
        boundProgram = lowerer.rewrite(boundProgram);

        if (boundProgram.hasErrors()) {
            for (BindingError error : boundProgram.getErrors()) {
                highlightBindingError(error, code.lines().collect(Collectors.toList()));
            }
        } else {

            JavaBytecodeCompiler compiler = new JavaBytecodeCompiler();
            compiler.compile(boundProgram, filename.split("\\.")[0]);

            Process process = Runtime.getRuntime().exec("java " + filename.split("\\.")[0] + " foo bar baz");
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

        String expectedResult = read("results/expected", filename.split("\\.")[0] + "_result.txt").trim();
        String actualResult = read("results/compilation", "jvm/" + filename.split("\\.")[0] + "_result.txt").trim();

        assertEquals(expectedResult, actualResult);

        if (!boundProgram.hasErrors()) {
            File classFile = new File(filename.split("\\.")[0] + ".class");
            assertTrue(classFile.delete(), "Could not delete classfile");
            File bytecodeFile = new File(filename.split("\\.")[0] + "_bytecode.txt");
            assertTrue(bytecodeFile.delete(), "Could not delete bytecode file");
        }
    }
}