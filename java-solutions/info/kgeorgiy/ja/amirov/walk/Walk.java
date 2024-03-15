package info.kgeorgiy.ja.amirov.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Set;

public class Walk {
    public static void walk(final String input, final String output, final int depth) throws WalkException {
        Path inputFile = validatePath(input);
        Path outputFile = validatePath(output);

        if (outputFile.getParent() != null) {
            try {
                Files.createDirectories(outputFile.getParent());
            } catch (IOException ignore) {
//                throw new WalkException(ioe.getMessage());
            }
        }

        try (BufferedReader bufferedReader = Files.newBufferedReader(inputFile)) {
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(outputFile)) {
                HashFileVisitor hashFileVisitor = new HashFileVisitor(bufferedWriter);
                String currentFilePath;
                while ((currentFilePath = bufferedReader.readLine()) != null) {
                    try {
                        Path validatedCurrentFilePath = validatePath(currentFilePath);
                        Files.walkFileTree(validatedCurrentFilePath, Set.of(), depth, hashFileVisitor);
                    } catch (FileSystemNotFoundException | SecurityException e) {
                        System.err.format("File %s is not found.%n%s%n", currentFilePath, e.getMessage());
                    } catch (WalkException e) {
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

    public static Path validatePath(String path) throws WalkException {
        try {
            return Path.of(path);
        } catch (InvalidPathException exception) {
            throw new WalkException(String.format("Invalid path to file %s.%nProgram finished with exception: %s%n", path, exception.getMessage()));
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Wrong args");
        } else {
            try {
                walk(args[0], args[1], 0);
            } catch (WalkException we) {
                System.err.println(we.getMessage());
            }
        }
    }
}
