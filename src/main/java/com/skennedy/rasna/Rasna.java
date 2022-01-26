package com.skennedy.rasna;

import com.skennedy.assertclauses.Assert;
import com.skennedy.flags.Flag;
import com.skennedy.flags.Flags;
import com.skennedy.rasna.compilation.CompileTarget;
import com.skennedy.rasna.compilation.Compiler;
import com.skennedy.rasna.compilation.CompilerFactory;
import com.skennedy.rasna.diagnostics.BindingError;
import com.skennedy.rasna.diagnostics.Error;
import com.skennedy.rasna.diagnostics.TextSpan;
import com.skennedy.rasna.graphing.HighLevelTreeGrapher;
import com.skennedy.rasna.graphing.LowLevelTreeGrapher;
import com.skennedy.rasna.lexing.model.Location;
import com.skennedy.rasna.lowering.BoundProgramRewriter;
import com.skennedy.rasna.lowering.JVMLowerer;
import com.skennedy.rasna.lowering.LowererFactory;
import com.skennedy.rasna.parsing.Parser;
import com.skennedy.rasna.parsing.Program;
import com.skennedy.rasna.simulation.Simulator;
import com.skennedy.rasna.typebinding.Binder;
import com.skennedy.rasna.typebinding.BindingWarning;
import com.skennedy.rasna.typebinding.BoundProgram;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

public class Rasna {

    public static final String FILE_EXT = "rasna";

    private static final Logger log = LogManager.getLogger(Rasna.class);

    public static void main(String[] args) throws IOException {
        Flag<String> modeFlag = Flags.stringFlag()
                .withName("m")
                .withDefaultValue("sim")
                .withDescription("The mode for the compiler, sim simulates the program, com compiles it. Default is sim")
                .build();
        Flag<String> fileFlag = Flags.stringFlag()
                .withName("f")
                .withDescription("The file to compile")
                .build();
        Flag<String> targetFlag = Flags.stringFlag()
                .withName("t")
                .withDefaultValue("jvm")
                .withDescription("The target for the compiler, possible values: {jvm, llvm}")
                .build();
        Flags.parse(args);

        Mode mode;
        if ("sim".equals(modeFlag.getValue())) {
            log.debug("Rasna is in simulation mode");
            mode = Mode.SIMULATION;
        } else if ("com".equals(modeFlag.getValue())) {
            log.debug("Rasna is in compilation mode");
            mode = Mode.COMPILATION;
        } else {
            Flags.usage();
            System.exit(1);
            return;
        }
        CompileTarget compileTarget = CompileTarget.fromId(targetFlag.getValue());

        String fileNameWithExt = (String) Assert.that(fileFlag.getValue()).isNotBlank().get();
        String[] fileParts = fileNameWithExt.split("\\.");
        String fileName = fileParts[0];
        String fileExt = fileParts[1];

        if (!FILE_EXT.equals(fileExt)) {
            throw new IllegalArgumentException("File must be a ." + FILE_EXT + " file. Instead got ." + fileExt);
        }

        Path path = Paths.get(fileNameWithExt).toAbsolutePath();
        try {
            Instant start = Instant.now();
            List<String> lines = Files.readAllLines(path);
            String code = String.join(StringUtils.LF, lines);

            Parser parser = new Parser();
            Program program = parser.parse(path, code);

            if (program.hasErrors()) {
                int errorSize = program.getErrors().size();
                System.err.println("Parsing failed with " + errorSize + (errorSize > 1 ? " errors:\n" : " error:\n"));
                for (Error error : program.getErrors()) {
                    System.err.println(error.getMessage());
                    //TODO: This breaks if the error is in an imported file, i.e. the error is not in `lines`
                    highlightError(error, lines);
                }
                return;
            }

            Binder binder = new Binder();
            BoundProgram boundProgram = binder.bind(program);

            if (boundProgram.hasWarnings()) {
                int warningSize = boundProgram.getWarnings().size();
                System.err.println("Compilation completed with " + warningSize + (warningSize > 1 ? " warnings:\n" : " warning:\n"));
                for (BindingWarning warning : boundProgram.getWarnings()) {
                    highlightBindingWarning(warning, lines);
                    try {
                        Thread.sleep(50); //For some reason without this the printing goes out of order...
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (boundProgram.hasErrors()) {
                int errorSize = boundProgram.getErrors().size();
                System.err.println("Compilation failed with " + errorSize + (errorSize > 1 ? " errors:\n" : " error:\n"));
                for (BindingError error : boundProgram.getErrors()) {
                    highlightBindingError(error, lines);
                    try {
                        Thread.sleep(50); //For some reason without this the printing goes out of order...
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return;
            }

            //TODO: make this a flag
            boolean graphProgram = false;
            //Write first in case of errors in compilation or simulation
            if (graphProgram) {
                log.debug("Writing high level AST graph");
                HighLevelTreeGrapher highLevelTreeGrapher = new HighLevelTreeGrapher();
                highLevelTreeGrapher.graphAST(boundProgram);
            }

            //Lower the program to a linear series of instructions
            BoundProgramRewriter rewriter = LowererFactory.getLowerer(compileTarget);
            boundProgram = rewriter.rewrite(boundProgram);

            if (graphProgram) {
                log.debug("Writing low level AST graph");
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
                    log.debug("Simulating program {}", fileNameWithExt);
                    Simulator simulator = new Simulator();
                    System.out.print(ConsoleColors.GREEN_BOLD_BRIGHT);
                    simulator.simulate(boundProgram);
                    System.out.print(ConsoleColors.RESET);
                    break;
                case COMPILATION:
                    log.debug("Compiling file {} for {}", fileNameWithExt, compileTarget.name());
                    Compiler compiler = CompilerFactory.create(compileTarget);
                    compiler.compile(boundProgram, fileName);
                    Instant end = Instant.now();
                    log.debug("Compiled in {}ms", end.toEpochMilli() - start.toEpochMilli());

                    //TODO: only do this with a -r flag

                    boolean run = false;
                    if (run) {
                        Process process;
                        start = Instant.now();
                        switch (compileTarget) {
                            case JVM:
                                process = Runtime.getRuntime().exec("java " + fileName);
                                break;
                            case LLVM:
                                //TODO: This is windows specific
                                process = Runtime.getRuntime().exec(fileName + ".exe");
                                break;
                            default:
                                throw new IllegalStateException("Unexpected compile target: " + compileTarget);
                        }
                        end = Instant.now();
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
                    }
            }

        } catch (IOException ioe) {
            log.error("Error reading input file", ioe);
            throw ioe;
        }
    }

    private static void highlightError(Error error, List<String> lines) {
        Location location = error.getLocation();
        int row = location.getRow();
        if (row > lines.size()) {
            return;
        }
        String line = lines.get(row);

        if (row > 0) {
            System.out.print(row - 1 + ": ");
            System.out.print(ConsoleColors.CYAN_BOLD);
            System.out.print(lines.get(row - 1));
            System.out.print(ConsoleColors.RESET);
            System.out.println();
        }
        System.out.print(ConsoleColors.RESET);

        System.out.print(row + ": ");

        char[] charArray = line.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (i < location.getColumn() || i > location.getColumn() + (String.valueOf(error.getToken().getValue())).length()-1) {
                System.out.print(ConsoleColors.CYAN_BOLD);
            } else {
                System.out.print(ConsoleColors.RED_BOLD);
            }
            System.out.print(c);
            System.out.print(ConsoleColors.RESET);
        }
        System.out.println();

        if (row < lines.size() - 1) {
            System.out.print(row + 1 + ": ");
            System.out.print(ConsoleColors.CYAN_BOLD);
            System.out.print(lines.get(row + 1));
            System.out.print(ConsoleColors.RESET);
            System.out.println();
        }
        System.out.print(ConsoleColors.RESET);
        System.out.println();
    }

    private static void highlightBindingWarning(BindingWarning warning, List<String> lines) {

        System.err.print(ConsoleColors.YELLOW_BOLD);
        System.err.println(warning.getMessage());
        highlightMessage(lines, warning.getSpan(), ConsoleColors.YELLOW_BOLD);
    }

    private static void highlightBindingError(BindingError error, List<String> lines) {

        System.err.print(ConsoleColors.RED);
        System.err.println(error.getMessage());
        highlightMessage(lines, error.getSpan(), ConsoleColors.RED_BOLD);
    }

    private static void highlightMessage(List<String> lines, TextSpan span, String color) {
        int row = span.getStart().getRow();
        String line = lines.get(row);

        System.out.print(ConsoleColors.RESET);
        if (row > 0) {
            System.out.print(row - 1 + ": ");
            System.out.print(ConsoleColors.CYAN_BOLD);
            System.out.print(lines.get(row - 1));
            System.out.println();
        }
        System.out.print(ConsoleColors.RESET);
        System.out.print(row + ": ");

        char[] charArray = line.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (i < span.getStart().getColumn() || i > span.getEnd().getColumn()) {
                System.out.print(ConsoleColors.CYAN_BOLD);
            } else {
                System.out.print(color);
            }
            System.out.print(c);
            System.out.print(ConsoleColors.RESET);
        }
        System.out.println();

        if (row < lines.size() - 1) {
            System.out.print(row + 1 + ": ");
            System.out.print(ConsoleColors.CYAN_BOLD);
            System.out.print(lines.get(row + 1));
            System.out.println();
        }
        System.out.print(ConsoleColors.RESET);
        System.out.println();
    }

    private enum Mode {
        SIMULATION,
        COMPILATION
    }

    //Yoinked from SO
    public class ConsoleColors {
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
