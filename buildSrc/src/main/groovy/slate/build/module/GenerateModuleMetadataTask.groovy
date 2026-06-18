package slate.build.module

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.gradle.internal.enterprise.test.FileProperty
import org.gradle.internal.enterprise.test.OutputFileProperty
import org.gradle.plugins.ide.eclipse.model.Output
import slate.build.dependency.ModuleDependenciesExtension
import slate.build.model.ModuleIDs

abstract class GenerateModuleMetadataTask extends DefaultTask {

    // The package component to generate for
    @Input
    PackageComponent component;

    @Internal
    final DirectoryProperty outputDirectory = project.objects.directoryProperty();

    @Internal
    final RegularFileProperty componentManifestFile = project.objects.fileProperty();

    @TaskAction
    void generate() {
        // create destination directory
        File subfolder = new File(
                outputDirectory.get().asFile,
                "META-INF/slate-module-metadata/"
        );

        if (subfolder.exists()) {
            subfolder.deleteDir()
        }

        subfolder.mkdirs()

        // collect dependencies for this scope
        def dependencyList = [ ]
        def dependencies = project.extensions.findByType(ModuleDependenciesExtension).collectApplicable(component.name)
        dependencies.each { dependency ->
            def qualifier = ModuleIDs.qualifierFromDependency(dependency.group, dependency.name, dependency.component)
            def qualifierHash = ModuleIDs.hashUntrimmed(qualifier)
            dependencyList.add(
                [
                        group: dependency.group,
                        name: dependency.name,
                        component: dependency.component,
                        qualifier: qualifier,
                        qualifierHash: qualifierHash,
                        version: dependency.version,
                        specifier: dependency.specifier,
                        provided: dependency.provided
                ] + dependency.properties
            );
        }

        // build final data struct as it will be saved to the resources
        def data = [
                group: project.group,
                name: project.name,
                packageQualifier: component.pkg.packageQualifier,
                component: component.name,
                componentQualifier: component.qualifier,

                qualifierHash: component.qualifierHash,
                version: project.version,
                uid: component.uniqueID,
                dependencies: dependencyList
        ]

        // add entry point info
        if (component.isApplication()) {
            data.put("entrypoint", component.entrypoint)
        }

        data.put("componentInfo", component.serializeAdditionalComponentData())

        // write to both uid.json and root.component.json
        def file = new File(subfolder, "${component.uniqueID}.json")
        file.text = JsonOutput.prettyPrint(JsonOutput.toJson(data))
        file = componentManifestFile.get().asFile
        file.text = JsonOutput.prettyPrint(JsonOutput.toJson(data))
    }
}