import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*

import java.security.MessageDigest

abstract class GenerateModuleMetadataTask extends DefaultTask {

    @Internal
    List<SlateDependencySpec> modules = []

    @OutputDirectory
    final DirectoryProperty outputDirectory =
            project.objects.directoryProperty()

    @TaskAction
    void generate() {
        def moduleExt = project.extensions.getByType(SlateModuleExtension)

        // export module dependency list
        def moduleList = [ ]

        File subfolder = new File(
                outputDirectory.get().asFile,
                "META-INF/slate-module-metadata/"
        );

        subfolder.mkdirs()

        modules.each { module ->

            def input =
                    "${module.group}:${module.name}:${module.version}"

            def hash =
                    MessageDigest.getInstance("SHA-256")
                            .digest(input.bytes)
                            .encodeHex()
                            .toString()
                            .substring(0, 20)

            moduleList.add(
                [
                        group: module.group,
                        name: module.name,
                        version: module.version,
                        specifier: module.specifier
                ] + module.properties
            );
        }

        def data = [
                group: project.group,
                name: project.name,
                "qual-hash": moduleExt.qualifierHash,
                "version": project.version,
                "uid": moduleExt.uniqueID,
                "module-dependencies": moduleList
        ]

        def file = new File(
                subfolder,
                "auto.${moduleExt.uniqueID}.json"
        )

        file.text = JsonOutput.prettyPrint(JsonOutput.toJson(data))

        def file2 = new File(
                subfolder,
                "auto.root.json"
        )

        if (moduleExt.isApplication()) {
            data.put("entrypoint", moduleExt.entrypoint)
        }

        file2.text = JsonOutput.prettyPrint(JsonOutput.toJson(data))
    }
}