package slate.build.module

import groovy.transform.ToString
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.util.internal.GUtil
import slate.build.PackageExtension
import slate.build.model.ModuleIDs

@ToString
class PackageComponent {

    transient PackageExtension pkg;
    String name; // The name of the component

    String qualifier;
    String uniqueID;
    String qualifierHash;
    String trimmedQualifierHash;

    String entrypoint; // The entrypoint if this is an application

    /* Transient evaluated objects */
    transient SourceSet sourceSet;

    transient TaskProvider<Jar> jarTask;
    transient TaskProvider<GenerateModuleMetadataTask> moduleMetadataTask;

    /* Additional Properties */
    List<String> deploymentScopes = new ArrayList<>();

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
