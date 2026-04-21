package com.codearena.judge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Sandbox {

    private static final Logger LOGGER = Logger.getLogger(Sandbox.class.getName());
    private static final Path BASE_DIR = Paths.get("/tmp/codearena");

    private Sandbox() {
    }

    public static Path createTempDir(int submissionId) throws IOException {
        Path directory = BASE_DIR.resolve(String.valueOf(submissionId));
        Files.createDirectories(directory);
        return directory;
    }

    public static void writeSourceFile(Path dir, String code) throws IOException {
        Files.writeString(dir.resolve("Solution.java"), code, StandardCharsets.UTF_8);
    }

    public static void cleanup(int submissionId) {
        Path directory = BASE_DIR.resolve(String.valueOf(submissionId));
        if (!Files.exists(directory)) {
            return;
        }

        try (var pathStream = Files.walk(directory)) {
            pathStream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    LOGGER.log(Level.WARNING, "Failed to delete sandbox path: " + path, exception);
                }
            });
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to clean sandbox for submission " + submissionId, exception);
        }
    }

    public static boolean exists(int submissionId) {
        return Files.exists(BASE_DIR.resolve(String.valueOf(submissionId)));
    }
}
