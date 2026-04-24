package model;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

public class AppLogger {

    private static final String LOG_DIR = System.getProperty("user.home") + "/.kennwertdatenbank/logs";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static Logger logger;
    private static LocalDate currentDate;

    private AppLogger() {
    }

    private static synchronized Logger getLogger() {
        LocalDate today = LocalDate.now();

        if (logger == null || !today.equals(currentDate)) {
            initLogger(today);
            currentDate = today;
        }
        return logger;
    }

    private static void initLogger(LocalDate date) {
        try {
            Path logDir = Paths.get(LOG_DIR);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

            String logFile = LOG_DIR + "/log-" + date.format(DATE_FORMAT) + ".log";

            if (logger != null) {
                for (Handler h : logger.getHandlers()) {
                    h.close();
                    logger.removeHandler(h);
                }
            }

            logger = Logger.getLogger("AppLogger");
            logger.setUseParentHandlers(false);

            FileHandler fileHandler = new FileHandler(logFile, true);
            fileHandler.setFormatter(new LogFormatter());
            fileHandler.setLevel(Level.ALL);
            logger.addHandler(fileHandler);

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new LogFormatter());
            consoleHandler.setLevel(Level.WARNING);
            logger.addHandler(consoleHandler);

            logger.setLevel(Level.ALL);

        } catch (IOException e) {
            System.err.println("Logger konnte nicht initialisiert werden: " + e.getMessage());
        }
    }

    public static void info(String message) {
        getLogger().info(message);
    }

    public static void warning(String message) {
        getLogger().warning(message);
    }

    public static void error(String message, Throwable throwable) {
        getLogger().log(Level.SEVERE, message, throwable);
    }

    public static void error(String message) {
        getLogger().severe(message);
    }

    public static void debug(String message) {
        getLogger().fine(message);
    }

    private static class LogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            sb.append("[")
                    .append(LocalDateTime.now().format(TIMESTAMP_FORMAT))
                    .append("] ")
                    .append(String.format("%-8s", record.getLevel().getName()))
                    .append(" | ")
                    .append(record.getMessage());

            // Exception-Stacktrace anhängen wenn vorhanden
            if (record.getThrown() != null) {
                sb.append("\n  Exception: ").append(record.getThrown());
                for (StackTraceElement el : record.getThrown().getStackTrace()) {
                    sb.append("\n    at ").append(el);
                }
            }
            sb.append("\n");
            return sb.toString();
        }
    }
}