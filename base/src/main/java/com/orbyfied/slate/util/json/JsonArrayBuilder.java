package com.orbyfied.slate.util.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.Collection;

public class JsonArrayBuilder extends JsonElementBuilder<JsonArray> {

    public JsonArrayBuilder(JsonArray element) {
        super(element);
    }

    public JsonArrayBuilder add(Object v) {
        if (v == null) {
            return this;
        }

        this.elem.add(Json.GSON.toJsonTree(v));
        return this;
    }

    public JsonArrayBuilder add(JsonElement element) {
        if (element == null) {
            return this;
        }

        this.elem.add(element);
        return this;
    }

    public JsonArrayBuilder add(JsonElementBuilder<?> builder) {
        if (builder == null) {
            return this;
        }

        this.elem.add(builder.build());
        return this;
    }

    public JsonArrayBuilder addAll(Object... v) {
        if (v == null) {
            return this;
        }

        for (Object e : v) add(e);
        return this;
    }

    public JsonArrayBuilder addAll(Collection<?> v) {
        if (v == null) {
            return this;
        }

        for (Object e : v) add(e);
        return this;
    }

    public JsonArrayBuilder addAll(JsonElement... element) {
        if (element == null) {
            return this;
        }

        for (JsonElement e : element) add(e);
        return this;
    }

    public JsonArrayBuilder addAll(JsonElementBuilder<?>... builder) {
        if (builder == null) {
            return this;
        }

        for (JsonElementBuilder<?> b : builder) add(b);
        return this;
    }

}
