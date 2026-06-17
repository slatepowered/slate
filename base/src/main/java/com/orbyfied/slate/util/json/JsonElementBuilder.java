package com.orbyfied.slate.util.json;

import com.google.gson.JsonElement;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class JsonElementBuilder<T extends JsonElement> {

    /** The aggregate element being built. */
    protected final T elem;

    /**
     * Builds/returns the final element instance.
     */
    public T build() {
        return elem;
    }

    public String jsonString() {
        return Json.GSON.toJson(build());
    }

}
