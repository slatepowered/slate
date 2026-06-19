package slate.build.component

import groovy.transform.ToString
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.util.internal.GUtil
import slate.build.packages.PackageExtension
import slate.build.model.ModuleIDs
import slate.build.task.GenerateModuleMetadataTask
import slate.build.util.LazyConfigurable
import slate.build.util.LazyConfigurableContainer

@ToString
class PackageComponent extends LazyConfigurable<PackageComponent> {

  transient PackageExtension pkg;
  String name; // The name of the component
  String qualifier;
  String uniqueID;
  String qualifierHash;
  String trimmedQualifierHash;

  boolean code = true // Whether this component has Java/Kotlin/compilable code, if disabled it will only process resources
  String entrypoint // The entrypoint if this is an application

  // Configuration Providers
  LazyConfigurableContainer<SourceSet> sourceSet = LazyConfigurableContainer.create();

  /* Transient evaluated objects */
  transient TaskProvider<Jar> jarTask
  transient TaskProvider<GenerateModuleMetadataTask> moduleMetadataTask

  /* Additional Properties */
  List<String> deploymentScopes = new ArrayList<>()

  void tryQualify(PackageExtension pkg) {
    this.pkg = pkg
    if (qualifier == null) {
      qualifier = isMain() ? pkg.packageQualifier : pkg.packageQualifier + "+" + name
    }

    qualifierHash = ModuleIDs.hashUntrimmed(qualifier)
    trimmedQualifierHash = ModuleIDs.hashToID(qualifierHash)
    if (uniqueID == null) {
      uniqueID = trimmedQualifierHash
    }
  }

  boolean isMain() {
    return name.equalsIgnoreCase("main")
  }

  boolean isApplication() {
    return entrypoint != null
  }

  String embeddedJarName() {
    return qualifier + "@" + pkg.version + ".jar"
  }

  String configurationName(String c) {
    return name + GUtil.toCamelCase(c)
  }

  void deploy(String... scopes) {
    deploymentScopes.addAll(Arrays.asList(scopes));
  }

  Map<String, Object> serializeAdditionalComponentData() {
    return [
        deploymentScopes: deploymentScopes
    ]
  }

}
