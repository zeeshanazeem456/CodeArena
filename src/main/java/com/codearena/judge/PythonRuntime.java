package com.codearena.judge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

final class PythonRuntime {

    private static final List<List<String>> CANDIDATES = List.of(
            List.of("python"),
            List.of("python3"),
            List.of("py", "-3")
    );

    private PythonRuntime() {
    }

    static String[] compileCommand(String sourceFile) {
        return commandWithArgs("-m", "py_compile", sourceFile);
    }

    static String[] runCommand(String sourceFile) {
        return commandWithArgs(sourceFile);
    }

    private static String[] commandWithArgs(String... args) {
        List<String> command = findAvailableCommand();
        if (command.isEmpty()) {
            return new String[0];
        }
        List<String> fullCommand = new ArrayList<>(command);
        fullCommand.addAll(List.of(args));
        return fullCommand.toArray(String[]::new);
    }

    private static List<String> findAvailableCommand() {
        for (List<String> candidate : CANDIDATES) {
            if (isAvailable(candidate)) {
                return candidate;
            }
        }
        return List.of();
    }

    private static boolean isAvailable(List<String> command) {
        List<String> checkCommand = new ArrayList<>(command);
        checkCommand.add("--version");
        try {
            Process process = new ProcessBuilder(checkCommand).redirectErrorStream(true).start();
            boolean finished = process.waitFor(3, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (IOException exception) {
            return false;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
