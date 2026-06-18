package com.orbyfied.slate.bootstrap;

import com.eclipsesource.json.JsonObject;
import lombok.Getter;
import lombok.ToString;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
  protected String component;
  protected String packageQualifier;
  protected String componentQualifier;
  protected String qualifierHash;
  protected String entrypoint;
  protected String uid;

  protected final List<URL> providedLibraries = new ArrayList<>();
  protected Object entryInstance;

  protected void loadRootMetadata(JsonObject ob) {
    this.group = ob.getString("group", null);
    this.name = ob.getString("name", null);
    this.version = ob.getString("version", null);
    this.component = ob.getString("component", null);
    this.packageQualifier = ob.getString("packageQualifier", null);
    this.componentQualifier = ob.getString("componentQualifier", null);
    this.qualifierHash = ob.getString("qualifierHash", null);
    this.uid = ob.getString("uid", null);

    this.entrypoint = ob.getString("entrypoint", null);
  }

  @SuppressWarnings("unchecked")
  public <T> T entryInstance() {
    return (T) entryInstance;
  }

  @SuppressWarnings("unchecked")
  public <T> T entryInstance(Class<T> tClass) {
    return (T) entryInstance;
  }

}
