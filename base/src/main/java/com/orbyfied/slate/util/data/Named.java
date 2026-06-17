package com.orbyfied.slate.util.data;

import java.util.Map;

@SuppressWarnings({ "unchecked", "rawtypes" })
public interface Named {

    /**
     * Get the primary name for this object.
     */
    String getName();

    /**
     * Get the defined aliases for this object.
     */
    default String[] getAliases() {
        return new String[0];
    }

    default void registerByNameAndAliases(Map map) {
        map.put(getName(), this);
        String[] aliases = getAliases();
        if (aliases != null) {
            for (String a : aliases) {
                map.put(a, this);
            }
        }
    }

    default void registerByNameAndAliasesLowercase(Map map) {
        map.put(getName().toLowerCase(), this);
        String[] aliases = getAliases();
        if (aliases != null) {
            for (String a : aliases) {
                map.put(a.toLowerCase(), this);
            }
        }
    }

}
