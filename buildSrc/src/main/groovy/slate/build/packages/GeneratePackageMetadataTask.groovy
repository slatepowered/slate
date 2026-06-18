package slate.build.packages

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import slate.build.PackageExtension

abstract class GeneratePackageMetadataTask extends DefaultTask {

    @OutputFile
    abstract RegularFileProperty getOutputFile()

    @TaskAction
    void generate() {
        def packageDef = project.extensions.findByType(PackageExtension)

        // build component data list
        def componentDataList = []
        for (def comp : packageDef.components) {
            def componentData = [
                    name: comp.name,
                    componentQualifier: comp.qualifier,
                    qualifierHash: comp.qualifierHash,
                    uid: comp.uniqueID,
                    embeddedJarName: comp.embeddedJarName(),
            ] + comp.serializeAdditionalComponentData()

            componentDataList.add(componentData)
        }

        // build root data object
        def data = [
                group: project.group,
                name: project.name,
                version: packageDef.version,
                packageQualifier: packageDef.packageQualifier,
                uid: packageDef.uniqueID,
                qualifierHash: packageDef.qualifierHash,
                components: componentDataList
        ]

        outputFile.get().asFile.parentFile.mkdirs();
        outputFile.get().asFile.text = JsonOutput.prettyPrint(JsonOutput.toJson(data))
    }

}
