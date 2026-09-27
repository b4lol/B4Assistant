package com.b4lol.assistant.utils;

import android.os.SystemClock;

import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class RootUtils {
    public static final int MODULE_MISSING_EXIT_CODE = 66;
    private static final long ROOT_CACHE_DURATION_MS = 60_000;
    private static final long COMMAND_TIMEOUT_SECONDS = 30;
    private static final int MAX_OUTPUT = 64 * 1024;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);
    private static volatile Boolean rootCached;
    private static volatile long rootCacheTime;

    private RootUtils() {}

    public static boolean hasRootAccess() {
        Boolean cached = rootCached;
        if (cached != null && SystemClock.elapsedRealtime() - rootCacheTime < ROOT_CACHE_DURATION_MS) {
            return cached;
        }
        boolean granted = runCommand("id -u").getOutput().trim().equals("0");
        rootCached = granted;
        rootCacheTime = SystemClock.elapsedRealtime();
        return granted;
    }

    public static void runAsync(Runnable task) {
        EXECUTOR.execute(task);
    }

    public static CommandResult runCommand(String command) {
        return runCommand(new String[]{command});
    }

    public static CommandResult runScript(String path) {
        String quoted = shellQuote(path);
        CommandResult result = runCommand("if [ ! -f " + quoted + " ]; then exit "
                + MODULE_MISSING_EXIT_CODE + "; fi\nsh " + quoted);
        if (result.isSuccess() || result.getExitCode() == MODULE_MISSING_EXIT_CODE) {
            rootCached = true;
            rootCacheTime = SystemClock.elapsedRealtime();
        }
        return result;
    }

    public static CommandResult runCommand(String[] commands) {
        Process process = null;
        StringBuilder output = new StringBuilder();
        int exitCode = -1;
        String error = "";
        try {
            process = new ProcessBuilder("su").redirectErrorStream(true).start();
            final Process running = process;
            Thread reader = new Thread(() -> {
                try (BufferedReader input = new BufferedReader(new InputStreamReader(running.getInputStream(), StandardCharsets.UTF_8))) {
                    char[] buffer = new char[1024];
                    int count;
                    while ((count = input.read(buffer)) != -1) {
                        synchronized (output) {
                            if (output.length() < MAX_OUTPUT) {
                                output.append(buffer, 0, Math.min(count, MAX_OUTPUT - output.length()));
                            }
                        }
                    }
                } catch (Exception ignored) { }
            }, "b4-root-output");
            reader.setDaemon(true);
            reader.start();
            try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
                for (String command : commands) {
                    writer.write(command);
                    writer.write('\n');
                }
                writer.write("exit\n");
            }
            if (process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                exitCode = process.exitValue();
            } else {
                process.destroyForcibly();
                error = "Root command timed out";
            }
            reader.join(1000);
        } catch (Exception e) {
            error = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
        } finally {
            if (process != null) process.destroy();
        }
        String captured = output.toString();
        return new CommandResult(exitCode, captured, error.isEmpty() && exitCode != 0 ? captured : error);
    }

    public static String shellQuote(String value) {
        return "'" + value.replace("'", "'\\''") + "'";
    }

    public static boolean fileExists(String path) {
        return runCommand("test -f " + shellQuote(path)).isSuccess();
    }

    public static boolean isExecutable(String path) {
        return runCommand("test -x " + shellQuote(path)).isSuccess();
    }

    public static void makeExecutable(String path) {
        runCommand("chmod +x " + shellQuote(path));
    }

    public static void collapseStatusBar() {
        runAsync(() -> runCommand("cmd statusbar collapse"));
    }

    public static final class CommandResult {
        private final int exitCode;
        private final String output;
        private final String error;

        public CommandResult(int exitCode, String output, String error) {
            this.exitCode = exitCode;
            this.output = output;
            this.error = error;
        }

        public int getExitCode() { return exitCode; }
        public String getOutput() { return output; }
        public String getError() { return error; }
        public boolean isSuccess() { return exitCode == 0; }
    }
}
