package info.kgeorgiy.ja.amirov.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.io.IOException;
import java.nio.file.*;

public class RecursiveWalk {
    private static void recursiveWalk(final String input, final String output) throws Exception {
        Path inputFile = validatePath(input);
        Path outputFile = validatePath(output);

        if (outputFile.getParent() != null) {
            Files.createDirectories(outputFile.getParent());
        }

        try (BufferedReader bufferedReader = Files.newBufferedReader(inputFile)) {
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(outputFile)) {
                HashFileVisitor hashFileVisitor = new HashFileVisitor(bufferedWriter);
                String currentFilePath;
                while ((currentFilePath = bufferedReader.readLine()) != null) {
                    try {
                        Path validatedCurrentFilePath = validatePath(currentFilePath);
                        Files.walkFileTree(validatedCurrentFilePath, hashFileVisitor);
                    } catch (FileSystemNotFoundException | SecurityException e) {
                        System.err.format("File %s is not found.%n%s%n", currentFilePath, e.getMessage());
                    } catch (Exception e) {
                        bufferedWriter.write(String.format("%08x %s%n", 0, currentFilePath));
                    }
                }
            } catch (IOException ioe) {
                System.err.format("IOException in output file: %s%n", ioe.getMessage());
            }
        } catch (IOException ioe) {
            System.err.format("IOException in input file: %s%n", ioe.getMessage());
        }
    }

    // :NOTE: модификатор доступа
    // :NOTE:  throws Exception
    static Path validatePath(String path) throws Exception {
        try {
            return Path.of(path);
        } catch (InvalidPathException exception) {
            throw new Exception(String.format("Invalid path to file %s.%nProgram finished with exception: %s%n", path, exception.getMessage()));
        }
    }
    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Wrong args");
        } else {
            try {
                recursiveWalk(args[0], args[1]);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}

