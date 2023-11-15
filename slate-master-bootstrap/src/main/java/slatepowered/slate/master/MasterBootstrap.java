package slatepowered.slate.master;

import slatepowered.slate.logging.JavaLoggerProvider;
import slatepowered.slate.logging.Logger;
import slatepowered.slate.logging.Logging;
import slatepowered.veru.config.Configuration;
import slatepowered.veru.config.YamlConfigParser;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The entry point/bootstrap for the master instance.
 */
public class MasterBootstrap {

    private static final Path CONFIG_PATH = Paths.get("./config.yml");
    private static final String CONFIG_DEFAULTS = "/config.defaults.yml";
    private static final Logger LOGGER = Logging.getLogger("MasterBootstrap");

    public static void main(String[] args) throws Throwable {
        // set up basic logging
        Logging.setProvider(new JavaLoggerProvider(Paths.get("./logs"),
                "[%1$tT] [%3$s] [%4$s] %5$s %n"));

        // todo: parse config and options
        //  make options replace config values
        //  then actually start the Master instance

        // load init configuration
        Configuration config = new Configuration().withParser(YamlConfigParser.standard());
        config.reloadOrDefaultThrowing(CONFIG_PATH, CONFIG_DEFAULTS);

        // parse args (config overrides)
    }

}
