package slatepowered.slate.master;

import slatepowered.slate.logging.JavaLoggerProvider;
import slatepowered.slate.logging.Logger;
import slatepowered.slate.logging.Logging;

import java.nio.file.Paths;

/**
 * The entry point/bootstrap for the master instance.
 */
public class MasterBootstrap {

    private static final Logger LOGGER = Logging.getLogger("MasterBootstrap");

    public static void main(String[] args) throws Throwable {
        // set up basic logging
        Logging.setProvider(new JavaLoggerProvider(Paths.get("./logs"),
                "[%1$tT] [%3$s] [%4$s] %5$s %n"));

        // todo: parse config and options
        //  make options replace config values
        //  then actually start the Master instance
    }

}
