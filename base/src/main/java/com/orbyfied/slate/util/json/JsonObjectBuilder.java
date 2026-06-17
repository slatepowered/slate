package com.orbyfied.slate.util.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonObjectBuilder extends JsonElementBuilder<JsonObject> {

    public JsonObjectBuilder(JsonObject element) {
        super(element);
    }

    public JsonObjectBuilder add(String key, Object v) {
        if (v == null) {
            return this;
        }

        this.elem.add(key, Json.GSON.toJsonTree(v));
        return this;
    }

    public JsonObjectBuilder add(String key, JsonElement element) {
        if (element == null) {
            return this;
        }

        this.elem.add(key, element);
        return this;
    }

    public JsonObjectBuilder add(String key, JsonElementBuilder<?> builder) {
        if (builder == null) {
            return this;
        }

        this.elem.add(key, builder.build());
        return this;
    }

    public JsonObjectBuilder addIdLong(String name, long id) {
        elem.addProperty(name + "Long", id);
        elem.addProperty(name, String.valueOf(id));
        return this;
    }

}
