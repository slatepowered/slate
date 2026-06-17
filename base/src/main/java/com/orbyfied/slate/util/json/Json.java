package com.orbyfied.slate.util.json;

import com.google.gson.*;

import java.util.Collection;

public class Json {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static JsonObjectBuilder objectBuilder() {
        return new JsonObjectBuilder(new JsonObject());
    }

    public static JsonArrayBuilder arrayBuilder() {
        return new JsonArrayBuilder(new JsonArray());
    }

    public static JsonArrayBuilder arrayOf(Object... o) {
        return arrayBuilder().addAll(o);
    }

    public static JsonArrayBuilder arrayOf(Collection<?> o) {
        return arrayBuilder().addAll(o);
    }

    public static JsonArrayBuilder arrayOf(JsonElement... element) {
        return arrayBuilder().addAll(element);
    }

    public static JsonArrayBuilder arrayOf(JsonElementBuilder<?>... element) {
        return arrayBuilder().addAll(element);
    }

}
