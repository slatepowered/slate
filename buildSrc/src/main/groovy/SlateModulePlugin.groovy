import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.bundling.Jar

import java.security.MessageDigest

class SlateModulePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def logger = new PluginLogger(project, SlateModulePlugin);

        def moduleExt = project.extensions.create("slateModule", SlateModuleExtension)

        project.afterEvaluate {
            def input = "${project.group}:${project.name}"
            def hash = moduleExt.qualifierHash =
                    MessageDigest.getInstance("SHA-256")
                            .digest(input.bytes)
                            .encodeHex()
                            .toString()
            moduleExt.trimmedQualifierHash = hash.substring(0, 20)
            if (moduleExt.uniqueID == null) {
                // generate unique module ID using hash
                moduleExt.uniqueID = moduleExt.trimmedQualifierHash
                logger.info("Unique module ID was undefined, using qualifier hash newUniqueID(" + moduleExt.uniqueID + ")")
            }
        }

        // register auto dependency extension
        def depExtension =
                project.extensions.create(
                        "slateDependencies",
                        ModuleDependenciesExtension
                )

        project.plugins.withType(JavaPlugin) {
            def generateTask =
                    project.tasks.register(
                            "generateModuleMetadata",
                            GenerateModuleMetadataTask
                    ) {
                        outputDirectory.set(project.layout.buildDirectory.dir("resources/main"))

                        doFirst {
                            depExtension.modules.clear()
                            depExtension.modules.addAll(depExtension.modules)

                            depExtension.modules.each { module ->
                                String cn = module.configurationName != null ? module.configurationName : "compileOnly";
                                if (module.isProjectRef()) {
                                    project.dependencies.add(cn, project.dependencies.project(path: module.projectRef.path))
                                } else {
                                    project.dependencies.add(cn, "${module.group}:${module.name}:${module.version}")
                                }
                            }
                        }
                    }

            project.tasks.named("processResources") {
                dependsOn(generateTask)

                def tokens = [
                        projectUID: moduleExt.uniqueID,
                        version: project.version
                ]

                inputs.properties(tokens)

                filesMatching("**/*") {
                    expand(tokens)
                }
            }
        }

        def bundled = project.configurations.create("bundled") {
            canBeResolved = true
            canBeConsumed = true
        }

        def packed = project.configurations.create("packed") {
            canBeResolved = true
            canBeConsumed = true
        }

        project.configurations.named("implementation").configure {
            it.extendsFrom(project.configurations.named("bundled").get())
            it.extendsFrom(project.configurations.named("packed").get())
        }

        project.afterEvaluate {
            if (moduleExt.isApplication()) {
                project.tasks.register("runJar", Exec) {
                    group = "application"
                    description = "Builds and runs the generated jar"

                    dependsOn(project.tasks.named("jar"))

                    doFirst {
                        def jarTask = project.tasks.named("jar").get()
                        def jarFile = jarTask.archiveFile.get().asFile

                        commandLine(
                                "java",
                                "-jar",
                                jarFile.absolutePath
                        )
                    }
                }
            }

            project.plugins.withType(JavaPlugin) {
                project.tasks.withType(Jar) {
                    dependsOn packed.getTaskDependencyFromProjectDependency(true, "jar")
                    dependsOn bundled.getTaskDependencyFromProjectDependency(true, "jar")

                    into('META-INF/bundled-libs') {
                        from project.configurations.bundled
                    }

                    from({
                        project.configurations.getByName("packed").incoming.artifactView {}.files.collect {
                            project.zipTree(it)
                        }
                    })

                    duplicatesStrategy = DuplicatesStrategy.FAIL

                    if (moduleExt.isApplication()) {
                        manifest {
                            attributes(
                                    "Main-Class": "com.orbyfied.slate.bootstrap.ApplicationBootstrap"
                            )
                        }
                    }
                }

                if (moduleExt.isApplication()) {
                    project.dependencies.add("packed", project.project(":application-bootstrap"))
                }
            }
        }
    }
}