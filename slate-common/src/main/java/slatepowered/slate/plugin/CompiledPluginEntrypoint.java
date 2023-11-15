package slatepowered.slate.plugin;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Represents a parsed entrypoint for a {@link SlatePlugin}.
 */
public interface CompiledPluginEntrypoint {

    /**
     * Whether this entrypoint should be loaded and executed
     * on this plugin manager.
     *
     * @param manager The plugin manager.
     * @return Whether it should be executed.
     */
    boolean isValid(SlatePlugin plugin, SlatePluginManager manager) throws Throwable;

    /**
     * Initially loads the entry point and should call any
     * method signaling that the plugin has been loaded.
     *
     * @param plugin The plugin.
     * @param manager The plugin manager.
     */
    void loadAndExecute(SlatePlugin plugin, SlatePluginManager manager) throws Throwable;

    /**
     * Only load and execute this entrypoint if all the given
     * predicates pass.
     *
     * @param predicates The predicates.
     * @return The secured entrypoint.
     */
    default CompiledPluginEntrypoint onlyIf(Predicate<SlatePlugin>... predicates) {
        return onlyIf(this, Arrays.asList(predicates));
    }

    /**
     * Only load and execute this entrypoint if all the given
     * predicates pass.
     *
     * @param predicates The predicates.
     * @return The secured entrypoint.
     */
    default CompiledPluginEntrypoint onlyIf(List<Predicate<SlatePlugin>> predicates) {
        return onlyIf(this, predicates);
    }

    static CompiledPluginEntrypoint onlyIf(CompiledPluginEntrypoint entrypoint,
                                           List<Predicate<SlatePlugin>> predicates) {
        return new CompiledPluginEntrypoint() {
            @Override
            public boolean isValid(SlatePlugin plugin, SlatePluginManager manager) throws Throwable {
                for (Predicate<SlatePlugin> predicate : predicates) {
                    if (!predicate.test(plugin)) {
                        return false;
                    }
                }

                return entrypoint.isValid(plugin, manager);
            }

            @Override
            public void loadAndExecute(SlatePlugin plugin, SlatePluginManager manager) throws Throwable {
                entrypoint.loadAndExecute(plugin, manager);
            }
        };
    }

}
