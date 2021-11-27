package com.skennedy.lazuli;

import com.skennedy.assertclauses.Assert;
import com.skennedy.lazuli.graphing.LowLevelTreeGrapher;
import com.skennedy.lazuli.simulation.Simulator;
import com.skennedy.lazuli.conversion.JavaBytecodeCompiler;
import com.skennedy.lazuli.diagnostics.Error;
import com.skennedy.lazuli.graphing.HighLevelTreeGrapher;
import com.skennedy.lazuli.lowering.BoundProgramRewriter;
import com.skennedy.lazuli.lowering.Lowerer;
import com.skennedy.lazuli.parsing.Parser;
import com.skennedy.lazuli.parsing.Program;
import com.skennedy.lazuli.typebinding.Binder;
import com.skennedy.lazuli.typebinding.BoundProgram;
import com.skennedy.flags.Flag;
import com.skennedy.flags.Flags;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

public class Lazuli {

    private static final String LZL_EXT = "lzl";

    private static final Logger log = LogManager.getLogger(Lazuli.class);

    public static void main(String[] args) throws IOException {
        Flag<String> modeFlag = Flags.stringFlag()
                .withName("m")
                .withDefaultValue("sim")
                .withDescription("The mode for the compiler, sim simulates the program, com compiles it. Default is sim")
                .build();
        Flag<String> fileFlag = Flags.stringFlag()
                .withName("f")
                .withDescription("The file to convert")
                .build();
        Flags.parse(args);

        Mode mode;
        if ("sim".equals(modeFlag.getValue())) {
            log.info("Lazuli is in simulation mode");
            mode = Mode.SIMULATION;
        } else if ("com".equals(modeFlag.getValue())) {
            log.info("Lazuli is in conversion mode");
            mode = Mode.COMPILATION;
        } else {
            Flags.usage();
            System.exit(1);
            return;
        }

        String fileNameWithExt = (String) Assert.that(fileFlag.getValue()).isNotBlank().get();
        String[] fileParts = fileNameWithExt.split("\\.");
        String fileName = fileParts[0];
        String fileExt = fileParts[1];

        if (!LZL_EXT.equals(fileExt)) {
            throw new IllegalArgumentException("File must be a ." + LZL_EXT + " file.");
        }

        Path path = Paths.get(fileNameWithExt);
        try {
            Instant start = Instant.now();
            String code = String.join(StringUtils.LF, Files.readAllLines(path));

            Parser parser = new Parser();
            Program program = parser.parse(code);

            if (program.hasErrors()) {
                for (Error error : program.getErrors()) {
                    System.err.println(error.getMessage() + " at " + error.getLocation() + " -> " + error.getToken());
                }
                return;
            }

            Binder binder = new Binder();
            BoundProgram boundProgram = binder.bind(program);

            if (boundProgram.hasErrors()) {
                for (Error error : boundProgram.getErrors()) {
                    System.err.println(error.getMessage() + " at " + error.getLocation() + " -> " + error.getToken());
                }
                return;
            }

            //TODO: make this a flag
            boolean graphProgram = false;
            //Write first in case of errors in conversion or simulation
            if (graphProgram) {
                log.info("Writing high level AST graph");
                HighLevelTreeGrapher highLevelTreeGrapher = new HighLevelTreeGrapher();
                highLevelTreeGrapher.graphAST(boundProgram);
            }

            //Lower the program to a linear series of instructions
            BoundProgramRewriter rewriter = new Lowerer();
            boundProgram = rewriter.rewrite(boundProgram);

            if (graphProgram) {
                log.info("Writing low level AST graph");
                LowLevelTreeGrapher lowLevelTreeGrapher = new LowLevelTreeGrapher();
                lowLevelTreeGrapher.graphAST(boundProgram);
            }

            boolean printProgram = false;
            if (printProgram) {
                System.out.print(ConsoleColors.PURPLE_BOLD);
                System.out.println(code);
                System.out.print(ConsoleColors.RESET);
            }

            switch (mode) {
                case SIMULATION:
                    log.info("Simulating program {}", fileNameWithExt);
                    Simulator simulator = new Simulator();
                    System.out.print(ConsoleColors.GREEN_BOLD_BRIGHT);
                    simulator.simulate(boundProgram);
                    System.out.print(ConsoleColors.RESET);
                    break;
                case COMPILATION:
                    log.info("Compiling program {}", fileNameWithExt);
                    JavaBytecodeCompiler javaBytecodeCompiler = new JavaBytecodeCompiler();
                    javaBytecodeCompiler.convert(boundProgram, fileName);
                    Instant end = Instant.now();
                    log.info("Compiled in {}ms", end.toEpochMilli() - start.toEpochMilli());

                    Process process = Runtime.getRuntime().exec("java " + fileName);
                    InputStream inputStream = process.getInputStream();
                    char c = (char) inputStream.read();
                    System.out.print(ConsoleColors.CYAN_BOLD);
                    while (c != '\uFFFF') {
                        System.out.print(c);
                        c = (char) inputStream.read();
                    }
                    System.out.print(ConsoleColors.RED_BOLD);
                    InputStream errorStream = process.getErrorStream();
                    c = (char) errorStream.read();
                    while (c != '\uFFFF') {
                        System.out.print(c);
                        c = (char) errorStream.read();
                    }
                    System.out.print(ConsoleColors.RESET);
                    break;
            }

        } catch (IOException ioe) {
            log.error("Error reading input file", ioe);
            throw ioe;
        }
    }

    private enum Mode {
        SIMULATION,
        COMPILATION
    }

    //Yoinked from SO
    private class ConsoleColors {
        // Reset
        public static final String RESET = "\033[0m";  // Text Reset

        // Regular Colors
        public static final String BLACK = "\033[0;30m";   // BLACK
        public static final String RED = "\033[0;31m";     // RED
        public static final String GREEN = "\033[0;32m";   // GREEN
        public static final String YELLOW = "\033[0;33m";  // YELLOW
        public static final String BLUE = "\033[0;34m";    // BLUE
        public static final String PURPLE = "\033[0;35m";  // PURPLE
        public static final String CYAN = "\033[0;36m";    // CYAN
        public static final String WHITE = "\033[0;37m";   // WHITE

        // Bold
        public static final String BLACK_BOLD = "\033[1;30m";  // BLACK
        public static final String RED_BOLD = "\033[1;31m";    // RED
        public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
        public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
        public static final String BLUE_BOLD = "\033[1;34m";   // BLUE
        public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
        public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
        public static final String WHITE_BOLD = "\033[1;37m";  // WHITE

        // Underline
        public static final String BLACK_UNDERLINED = "\033[4;30m";  // BLACK
        public static final String RED_UNDERLINED = "\033[4;31m";    // RED
        public static final String GREEN_UNDERLINED = "\033[4;32m";  // GREEN
        public static final String YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
        public static final String BLUE_UNDERLINED = "\033[4;34m";   // BLUE
        public static final String PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
        public static final String CYAN_UNDERLINED = "\033[4;36m";   // CYAN
        public static final String WHITE_UNDERLINED = "\033[4;37m";  // WHITE

        // Background
        public static final String BLACK_BACKGROUND = "\033[40m";  // BLACK
        public static final String RED_BACKGROUND = "\033[41m";    // RED
        public static final String GREEN_BACKGROUND = "\033[42m";  // GREEN
        public static final String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
        public static final String BLUE_BACKGROUND = "\033[44m";   // BLUE
        public static final String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
        public static final String CYAN_BACKGROUND = "\033[46m";   // CYAN
        public static final String WHITE_BACKGROUND = "\033[47m";  // WHITE

        // High Intensity
        public static final String BLACK_BRIGHT = "\033[0;90m";  // BLACK
        public static final String RED_BRIGHT = "\033[0;91m";    // RED
        public static final String GREEN_BRIGHT = "\033[0;92m";  // GREEN
        public static final String YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
        public static final String BLUE_BRIGHT = "\033[0;94m";   // BLUE
        public static final String PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
        public static final String CYAN_BRIGHT = "\033[0;96m";   // CYAN
        public static final String WHITE_BRIGHT = "\033[0;97m";  // WHITE

        // Bold High Intensity
        public static final String BLACK_BOLD_BRIGHT = "\033[1;90m"; // BLACK
        public static final String RED_BOLD_BRIGHT = "\033[1;91m";   // RED
        public static final String GREEN_BOLD_BRIGHT = "\033[1;92m"; // GREEN
        public static final String YELLOW_BOLD_BRIGHT = "\033[1;93m";// YELLOW
        public static final String BLUE_BOLD_BRIGHT = "\033[1;94m";  // BLUE
        public static final String PURPLE_BOLD_BRIGHT = "\033[1;95m";// PURPLE
        public static final String CYAN_BOLD_BRIGHT = "\033[1;96m";  // CYAN
        public static final String WHITE_BOLD_BRIGHT = "\033[1;97m"; // WHITE

        // High Intensity backgrounds
        public static final String BLACK_BACKGROUND_BRIGHT = "\033[0;100m";// BLACK
        public static final String RED_BACKGROUND_BRIGHT = "\033[0;101m";// RED
        public static final String GREEN_BACKGROUND_BRIGHT = "\033[0;102m";// GREEN
        public static final String YELLOW_BACKGROUND_BRIGHT = "\033[0;103m";// YELLOW
        public static final String BLUE_BACKGROUND_BRIGHT = "\033[0;104m";// BLUE
        public static final String PURPLE_BACKGROUND_BRIGHT = "\033[0;105m"; // PURPLE
        public static final String CYAN_BACKGROUND_BRIGHT = "\033[0;106m";  // CYAN
        public static final String WHITE_BACKGROUND_BRIGHT = "\033[0;107m";   // WHITE
    }
}
