package slatepowered.slate.master;

import com.eclipsesource.json.JsonValue;
import lombok.Builder;
import slatepowered.slate.communication.CommunicationStrategy;
import slatepowered.slate.communication.RMQCommunicationStrategy;
import slatepowered.slate.logging.JavaLoggerProvider;
import slatepowered.slate.logging.Logger;
import slatepowered.slate.logging.Logging;
import slatepowered.slate.model.Network;
import slatepowered.veru.config.Configuration;
import slatepowered.veru.config.Section;
import slatepowered.veru.config.YamlConfigParser;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

/**
 * The entry point/bootstrap for the master instance.
 */
@Builder
public class MasterBootstrap {

    static {
        // filter out SLF4J warnings because they look bad
        // and i couldn't give less of a fuck
        PrintStream filterOut = new PrintStream(System.err) {
            public void println(String l) {
                if (l == null || !l.startsWith("SLF4J")) {
                    super.println(l);
                }
            }
        };

        System.setErr(filterOut);

        // set up basic logging
        Logging.setDebug(true);
        Logging.setProvider(new JavaLoggerProvider(Paths.get("./logs"),
                "[%1$tT] [%3$s] [%4$s] %5$s %n"));
    }

    private static final Logger LOGGER = Logging.getLogger("MasterBootstrap");

    private final Path configurationPath;               // The path you can find the configuration file at
    private final String configurationDefaultsResource; // The configuration defaults resource

    private Configuration config; // The configuration loaded into memory
    private Master master;        // The master network object

    public static void main(String[] args) {
        MasterBootstrap bootstrap = MasterBootstrap.builder()
                .configurationPath(Paths.get("./config.yml"))
                .configurationDefaultsResource("/config.defaults.yml")
                .build();

        bootstrap.start(args);
        System.out.println(bootstrap.getMaster().local().getService(Network.KEY));
        bootstrap.awaitClose();
    }

    public void start(String[] args) {
        // todo: parse config and options
        //  make options replace config values
        //  then actually start the Master instance

        // load init configuration
        LOGGER.info("Loading configuration and command line overrides");
        if (config == null) {
            config = new Configuration().withParser(YamlConfigParser.standard());
            config.reloadOrDefaultThrowing(configurationPath, configurationDefaultsResource);
        }

        // parse args (config overrides)
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            // check for option
            if (arg.startsWith("--")) {
                String pathStr = arg.substring(3);
                String valueStr = args[++i];
                Object value = JsonValue.readFrom(valueStr); // todo: maybe better parsing

                config.set(Section.path(pathStr), value);
                continue;
            }

            // check for boolean flag
            if (arg.startsWith("++")) {
                String pathStr = arg.substring(3);
                config.set(Section.path(pathStr), true);
                continue;
            }
        }

        // connect to communication services
        CommunicationStrategy communicationStrategy;
        String strategyName = config.get("communication", "strategy");
        LOGGER.info("Connecting to comm service with strategy(" + strategyName + ")");
        switch (strategyName) {
            case "rabbitmq":
            case "rmq":
                communicationStrategy = connectRabbitMQ(config.section("communication", "rabbitmq"));
                break;
            default:
                throw new IllegalArgumentException("Invalid communication strategy name: " + strategyName);
        }

        // create master network
        LOGGER.info("Bootstrapping network controller");
        master = new Master(
                Paths.get(config.getOrDefault("directory", "./")),
                communicationStrategy.createKey(),
                communicationStrategy
        );

        // load plugins from directory
        String pluginDir = config.getOrDefault("pluginsDirectory", "plugins");
        LOGGER.info("Loading plugins from directory(" + pluginDir + ")");
        master.getPluginManager().constructPluginsFromDirectory(master.getDirectory().resolve(pluginDir));
        master.getPluginManager().loadAll();

        // create shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down");
            master.destroy();
        }, "Shutdown thread"));

        // initialize plugins
        LOGGER.info("Initializing plugins count(" + master.getPluginManager().getPlugins().size() + ")");
        master.getPluginManager().initialize(master);

        // initialize integrated cluster instance
        if (config.<Boolean>issue("integrated-cluster", "enabled").orElse(true)) {
            LOGGER.info("Initializing integrated cluster");
            master.getPluginManager().initialize(master.getIntegratedCluster());
            master.getIntegratedCluster().setEnabled(true);

            // todo: maybe standardize cluster config parsing

            // make sure not to override an allocation checker set by plugins
            if (master.getIntegratedCluster().getAllocationChecker() == null) {
                int maxNodes = config.<Integer>issue("integrated-cluster", "max-nodes").orElse(5);
                master.getIntegratedCluster().setAllocationChecker((cluster, clusterInstance, name, parent, tags) -> {
                    return cluster.getLocalAllocations().size() < maxNodes;
                });
            }
        }
    }

    public void awaitClose() {
        // await network close
        master.onClose().await().join();
    }

    /** Starts a non-daemon thread which waits for the network to be destroyed. */
    public CompletableFuture<Void> awaitCloseAsync() {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        new Thread(() -> {
            awaitClose();
            future.complete(null);
        }, "Network Close Listener");

        return future;
    }

    // Connect to a RabbitMQ and create the communication strategy
    private static CommunicationStrategy connectRabbitMQ(Section config) {
        return RMQCommunicationStrategy.builder("master")
                .host(config.get("host"))
                .port(config.get("port"))
                .username(config.get("username"))
                .password(config.get("password"))
                .virtualHost(config.get("vhost"))
                .build();
    }

    public Master getMaster() {
        return master;
    }

    public Configuration getConfiguration() {
        return config;
    }

}
