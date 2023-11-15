package slatepowered.slate.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.logging.*;

/**
 * Uses the {@link java.util.logging} package and it's classes for
 * simple logging capabilities.
 */
public class JavaLoggerProvider extends LoggerProvider {

    // the log directory
    final Path logDirectory;

    // the log handlers
    FileHandler logFileHandler;
    ConsoleHandler consoleHandler;

    // the formatting of the logger
    final String format;

    public JavaLoggerProvider(Path logDirectory, String format) {
        this.logDirectory = logDirectory;
        this.format = format;

        // set up file logging if the provided log directory is present
        if (logDirectory == null) {
            logFileHandler = null;
            return;
        }
        
        boolean success;

        // try and close latest
        closeLatestLog();

        // create log directory
        // and latest.log
        Path latest = null;
        try {
            // create log directory
            if (!Files.exists(logDirectory))
                Files.createDirectories(logDirectory);

            // create latest.log
            latest = logDirectory.resolve("latest.log");
            if (!Files.exists(latest))
                Files.createFile(latest);
            success = true;
        } catch (Exception e) {
            System.err.println("Failed to create ./logs and latest.log");
            e.printStackTrace();
            success = false;
        }

        if (success) {
            try {
                Formatter formatter = new ConfigurableSimpleFormatter().format(this.format);

                // create file handler
                logFileHandler = new FileHandler(latest.toString());
                logFileHandler.setFormatter(formatter);
                logFileHandler.setEncoding(StandardCharsets.UTF_8.name());

                // create console handler
                consoleHandler = new ConsoleHandler();
                consoleHandler.setFormatter(formatter);
                consoleHandler.setEncoding(StandardCharsets.UTF_8.name());

                // add shutdown hook
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    // close log file
                    logFileHandler.close();
                }));
            } catch (Exception e) {
                System.err.println("Failed to init file handler");
                e.printStackTrace();
            }
        }
    }

    // renames the latest log file
    private void closeLatestLog() {
        final Path latest = logDirectory.resolve("latest.log");

        try {
            if (!Files.exists(latest)) return;

            // get name of file
            String fn = new Date().toString()
                    .replace(' ', '_')
                    .replace(':', '-')
                    .toLowerCase() + ".log";
            final Path dest = logDirectory.resolve(fn);

            // move file
            Files.move(latest, dest);
        } catch (Exception e) {
            System.err.println("Failed to close latest.log");
            e.printStackTrace();
            try { Files.delete(latest); } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * A wrapped java.util.logging logger.
     */
    final class JavaLogger extends AbstractLogger {
        // The java.util.logging logger it delegates to
        final java.util.logging.Logger logger;

        public JavaLogger(String name, java.util.logging.Logger logger) {
            super(name);
            this.logger = logger;
            logger.setUseParentHandlers(false);

            // set up handlers
            logger.addHandler(consoleHandler);
            logger.addHandler(logFileHandler);
        }

        @Override
        public void info(Object... msg) {
            logger.info(stringify(msg));
        }

        @Override
        public void warn(Object... msg) {
            logger.warning(stringify(msg));
        }

        @Override
        public void error(Object... msg) {
            logger.warning("Error: " + stringify(msg));
        }

        @Override
        public void severe(Object... msg) {
            logger.severe(stringify(msg));
        }

        @Override
        public void fatal(Object... msg) {
            logger.severe("FATAL: " + stringify(msg));
        }

        @Override
        public void debug(Object... msg) {
            if (Logging.DEBUG) logger.info("DEBUG: " + stringify(msg));
        }
    }

    @Override
    protected Logger createLogger(String name) {
        return new JavaLogger(name, java.util.logging.Logger.getLogger(name));
    }

    // A copy of java.util.logging.SimpleFormatter which
    // is configurable with a format per instance bc
    // for some reason the java impl doesnt offer that
    static class ConfigurableSimpleFormatter extends Formatter {
        /**
         * The format to use.
         */
        String format;

        public ConfigurableSimpleFormatter format(String format) {
            this.format = format;
            return this;
        }

        @Override
        public String format(LogRecord record) {
            ///////////////////////////////////////////////
            ///// I basically just copied this code
            ///// from java.util.logging.SimpleFormatter
            ///////////////////////////////////////////////

            ZonedDateTime zdt = ZonedDateTime.ofInstant(
                    Instant.ofEpochMilli(record.getMillis()), ZoneId.systemDefault());
            String source;
            if (record.getSourceClassName() != null) {
                source = record.getSourceClassName();
                if (record.getSourceMethodName() != null) {
                    source += " " + record.getSourceMethodName();
                }
            } else {
                source = record.getLoggerName();
            }

            String message = formatMessage(record);
            String throwable = "";
            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println();
                record.getThrown().printStackTrace(pw);
                pw.close();
                throwable = sw.toString();
            }

            return String.format(format,
                    zdt,
                    source,
                    record.getLoggerName(),
                    record.getLevel().getLocalizedName(),
                    message,
                    throwable);
        }
    }

}
