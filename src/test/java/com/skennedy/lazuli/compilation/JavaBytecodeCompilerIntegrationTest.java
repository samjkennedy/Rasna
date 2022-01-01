package com.skennedy.lazuli.compilation;

import com.skennedy.lazuli.conversion.JavaBytecodeCompiler;
import com.skennedy.lazuli.lowering.Lowerer;
import com.skennedy.lazuli.parsing.Parser;
import com.skennedy.lazuli.parsing.Program;
import com.skennedy.lazuli.typebinding.Binder;
import com.skennedy.lazuli.typebinding.BoundProgram;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaBytecodeCompilerIntegrationTest {


    @ParameterizedTest
    @MethodSource("getFilesToTest")
    void runFile_producesCorrectOutput(String filename) throws IOException {

        PrintStream console = System.out;
        //Set up output stream
        //TODO: figure out how to delete this
        File outputFile = new File("src/test/resources/results/compilation/" + filename.split("\\.")[0] + "_result.txt");
        outputFile.createNewFile();
        PrintStream out = new PrintStream(outputFile);
        // Store current System.out before assigning a new value
        //Set output to write to file
        System.setOut(out);

        String code = read("tests", filename);

        Parser parser = new Parser();
        Program program = parser.parse(code);

        Binder binder = new Binder();
        BoundProgram boundProgram = binder.bind(program);

        Lowerer lowerer = new Lowerer();
        boundProgram = lowerer.rewrite(boundProgram);

        JavaBytecodeCompiler compiler = new JavaBytecodeCompiler();
        compiler.convert(boundProgram, filename.split("\\.")[0]);

        Process process = Runtime.getRuntime().exec("java " + filename.split("\\.")[0]);
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
        //Reset console
        System.setOut(console);

        String expectedResult = read("results/expected", filename.split("\\.")[0] + "_result.txt");
        String actualResult = read("results/compilation", filename.split("\\.")[0] + "_result.txt");

        assertEquals(expectedResult, actualResult);
    }


    private String read(String folder, String filename) throws IOException {
        String path = "src/test/resources/" + folder + "/" + filename;
        File file = new File(path);
        return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    }

    private static Stream<String> getFilesToTest() {
        File folder = new File("src/test/resources/tests/");
        File[] listOfFiles = folder.listFiles();

        return Arrays.stream(listOfFiles)
                .filter(file -> file.getName().endsWith(".lzl"))
                .map(File::getName);
    }
}