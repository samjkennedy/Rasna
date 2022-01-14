package com.skennedy.rasna.compilation;

import com.skennedy.rasna.Rasna;
import com.skennedy.rasna.diagnostics.BindingError;
import com.skennedy.rasna.diagnostics.Error;
import com.skennedy.rasna.diagnostics.TextSpan;
import com.skennedy.rasna.lexing.model.Location;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class CompilerBaseIntegrationTest {

    public static void highlightError(Error error, List<String> lines) {
        System.out.println(error.getMessage());
        Location location = error.getLocation();
        int row = location.getRow();
        if (row > lines.size()) {
            return;
        }
        String line = lines.get(row);

        if (row > 0) {
            System.out.print(row - 1 + ": ");
            System.out.print(lines.get(row - 1));
            System.out.println();
        }

        System.out.print(row + ": ");

        System.out.println(line);

        if (row < lines.size() - 1) {
            System.out.print(row + 1 + ": ");
            System.out.print(lines.get(row + 1));
            System.out.println();
        }
        System.out.println();
    }

    public static void highlightBindingError(BindingError error, List<String> lines) {

        System.out.println(error.getMessage());
        highlightMessage(lines, error.getSpan());
    }

    public static void highlightMessage(List<String> lines, TextSpan span) {
        int row = span.getStart().getRow();
        String line = lines.get(row);

        if (row > 0) {
            System.out.println(((row - 1) + ": " + lines.get(row - 1)).trim());
        }
        System.out.print(row + ": ");

        for (char c : line.toCharArray()) {
            System.out.print(c);
        }
        System.out.println();

        if (row < lines.size() - 1) {
            System.out.println(((row + 1) + ": " + lines.get(row + 1)).trim());
        }
        System.out.println();
    }


    public String read(String folder, String filename) throws IOException {
        File file = new File(getFullPath(folder, filename));
        return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    }

    public String getFullPath(String folder, String filename) {
        return "src/test/resources/" + folder + "/" + filename;
    }

    public static Stream<String> getFilesToTest() {
        File folder = new File("src/test/resources/tests/");
        File[] listOfFiles = folder.listFiles();

        return Arrays.stream(listOfFiles)
                .filter(file -> file.getName().endsWith(Rasna.FILE_EXT))
                .map(File::getName);
    }
}
