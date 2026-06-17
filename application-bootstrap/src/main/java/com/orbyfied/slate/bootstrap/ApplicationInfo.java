package com.orbyfied.slate.bootstrap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.ToString;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Publicly exported information about the current application running,
 * provided as a singleton by {@link ApplicationBootstrap}.
 */
@Getter
@ToString(exclude = { "entryInstance", "rootMetadataObject", "extractedBundledLibraries" })
public class ApplicationInfo {

  protected JsonObject rootMetadataObject;

  protected String name;
  protected String group;
  protected String version;
  protected String qualifierHash;
  protected String entrypoint;

  protected final List<URL> extractedBundledLibraries = new ArrayList<>();
  protected Object entryInstance;

  protected void loadRootMetadata(JsonObject ob) {
    this.group = ob.get("group").getAsString();
    this.name = ob.get("name").getAsString();
    this.version = getStringOrNull(ob, "version");
    this.qualifierHash = ob.get("qual-hash").getAsString();

    this.entrypoint = getStringOrNull(ob, "entrypoint");
  }

  @SuppressWarnings("unchecked")
  public <T> T entryInstance() {
    return (T) entryInstance;
  }

  @SuppressWarnings("unchecked")
  public <T> T entryInstance(Class<T> tClass) {
    return (T) entryInstance;
  }

  private static <T> T getOrNull(JsonObject ob, String name, Function<JsonElement, T> function) {
    return ob.has(name) ? function.apply(ob.get(name)) : null;
  }

  private static String getStringOrNull(JsonObject ob, String name) {
    return ob.has(name) ? ob.get(name).getAsString() : null;
  }

}
