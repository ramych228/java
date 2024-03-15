package info.kgeorgiy.ja.amirov.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class HashFileVisitor extends SimpleFileVisitor<Path> {
    private final BufferedWriter writer;
    private static final int BUF_SIZE = 4096;

    HashFileVisitor(final BufferedWriter writer) {
        this.writer = writer;
    }

    @Override
    public FileVisitResult visitFile(Path filePath, BasicFileAttributes bfa) throws IOException {
        int hash = 0;
        try (InputStream reader = Files.newInputStream(filePath)) {
            int cnt;
            byte[] buf = new byte[BUF_SIZE];
            while ((cnt = reader.read(buf)) != -1) {
                for (int i = 0; i < cnt; i++) {
                    hash += buf[i] & 0xff;
                    hash += hash << 10;
                    hash ^= hash >>> 6;
                }
            }
            hash += hash << 3;
            hash ^= hash >>> 11;
            hash += hash << 15;
        } catch (IOException e) {
            hash = 0;
        }

        try {
            writer.write(String.format("%08x %s%n", hash, filePath));
        } catch (IOException ioe) {
            System.err.format("IOException in output file: %s%n", ioe.getMessage());
        }


        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path filePath, IOException ioe) throws IOException {
        writer.write(String.format("%08x %s%n", 0, filePath.toString()));
        return FileVisitResult.CONTINUE;
    }

}
