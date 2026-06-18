package slate.build

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.util.internal.GUtil
import slate.build.dependency.ModuleDependenciesExtension
import slate.build.model.ModuleIDs
import slate.build.module.GenerateModuleMetadataTask
import slate.build.module.PackageComponent
import slate.build.packages.GeneratePackageMetadataTask

class SlateBuildPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def logger = new PluginLogger(project, SlateBuildPlugin);

        def packageDef = project.extensions.create("slatePackage", PackageExtension)

        // preconfiguration with defaults
        def mainComponent = packageDef.component("main")

        // Main block for package configuration processing
        project.afterEvaluate {
            packageDef.version = project.version

            // create package name if not set
            if (packageDef.packageQualifier == null) {
                packageDef.packageQualifier = project.group + "." + project.name
                logger.info("Package name was unset, using group and name for: " + packageDef.packageQualifier)
            }

            // package qualifier hash
            def hash = packageDef.qualifierHash = ModuleIDs.hashUntrimmed(packageDef.packageQualifier)
            packageDef.trimmedQualifierHash = ModuleIDs.hashToID(hash);
            if (packageDef.uniqueID == null) {
                // generate unique module ID using hash
                packageDef.uniqueID = packageDef.trimmedQualifierHash
                logger.info("Unique package ID was undefined, using qualifier hash newUniqueID(" + packageDef.uniqueID + ")")
            }

            // create common packed and bundled dependency configurations
            def packedCommon = project.configurations.create("packed")
            def bundledCommon = project.configurations.create("bundled")

            // register all defined components
            SourceSetContainer ssc = project.extensions.getByType(SourceSetContainer)
            for (PackageComponent component : packageDef.components) {
                component.tryQualify(packageDef)

                // create and configure source sets
                SourceSet sourceSet = ssc.maybeCreate(component.name)
                sourceSet.java.srcDir("src/" + component.name + "/java")
                sourceSet.resources.srcDir("src/" + component.name + "/resources")
                component.sourceSet = sourceSet

                // create dependency configurations
                def packedConf = project.configurations.create(component.configurationName("packed"))
                def bundledConf = project.configurations.create(component.configurationName("bundled"))

                project.configurations.named(sourceSet.getImplementationConfigurationName()).configure {
                    it.extendsFrom(packedConf)
                    it.extendsFrom(bundledConf)

                    it.extendsFrom(packedCommon)
                    it.extendsFrom(bundledCommon)
                }

                packedConf.extendsFrom(packedCommon)
                bundledConf.extendsFrom(bundledCommon)
            }
        }

        // register auto dependency extension
        def depExtension =
                project.extensions.create(
                        "slateDependencies",
                        ModuleDependenciesExtension
                )

        /* Task and configuration setup, a lot of iteration over components occurs here */
        project.afterEvaluate { project.plugins.withType(JavaPlugin) {
            println(packageDef.components)
            // configure dependencies on runtime and compiler classpath
            depExtension.dependencies.each { module ->
                String configBaseName = module.configurationName != null ? module.configurationName : "compileOnly";
                String configName = module.scope == null ? configBaseName : module.scope + GUtil.toCamelCase(configBaseName);

                if (module.isProjectRef()) {
                    project.dependencies.add(configName, project.dependencies.project(path: module.projectRef.path))
                } else {
                    project.dependencies.add(configName, "${module.group}:${module.name}:${module.version}")
                }
            }

            // for each component, configure the metadata generation tasks,
            // the processResources task and the jar tasks
            for (def comp : packageDef.components) {
                def generateTask = comp.moduleMetadataTask =
                        project.tasks.register(
                                comp.sourceSet.getTaskName("generate", "ComponentMetadata"),
                                GenerateModuleMetadataTask) {
                            it.component = comp
                            it.outputDirectory.set(project.layout.buildDirectory.dir("resources/" + comp.name))
                            it.componentManifestFile.set(it.outputDirectory.file("META-INF/slate-module-metadata/component.json"))
                        }

                project.tasks.withType(ProcessResources).named(comp.sourceSet.getProcessResourcesTaskName()) {
                    it.dependsOn(generateTask)

                    // configure replacement of tokens of the form `${token}` in resources
                    def tokens = [
                            packageUID: packageDef.uniqueID,
                            componentUID: comp.uniqueID,
                            packageQualifier: packageDef.packageQualifier,
                            componentQualifier: comp.qualifier,
                            componentName: comp.name,
                            version: project.version
                    ]

                    it.inputs.properties(tokens)
                    it.filesMatching("**/*") {
                        it.expand(tokens)
                    }
                }

                project.tasks.withType(Jar).named(comp.sourceSet.getJarTaskName()) {
                    if (!comp.isMain()) {
                        it.archiveClassifier.set(comp.name)
                    }
                }

                // if the component is an application (has an entry point)
                if (comp.isApplication()) {
                    // add the run jar task for testing
                    project.tasks.register(comp.sourceSet.getTaskName("run", "Jar"), Exec) {
                        group = "application"
                        description = "Builds and runs the generated jar of the `" + comp.name + "` component"

                        dependsOn(comp.sourceSet.getJarTaskName())

                        doFirst {
                            def jarTask = project.tasks.named(comp.sourceSet.getJarTaskName()).get()
                            def jarFile = jarTask.archiveFile.get().asFile

                            commandLine(
                                    "java",
                                    "-jar",
                                    jarFile.absolutePath
                            )
                        }
                    }

                    // important: add a packed dependency, untracked by the module system,
                    // for the common application bootstrap
                    project.dependencies.add(comp.configurationName("packed"), 'com.eclipsesource.minimal-json:minimal-json:0.9.5')
                    project.dependencies.add(comp.configurationName("packed"), project.project(":application-bootstrap"))
                }
            }

            // configure bundled and packed dependencies on each of the jar tasks
            // the default dependency configurations are common to all components
            for (def comp : packageDef.components) {
                def bundledConf = project.configurations.named(comp.configurationName("bundled")).get()
                def packedConf = project.configurations.named(comp.configurationName("packed")).get()

                (comp.jarTask = project.tasks.withType(Jar).named(comp.sourceSet.getJarTaskName())).configure {
                    it.dependsOn packedConf.getTaskDependencyFromProjectDependency(true, it.name)
                    it.dependsOn bundledConf.getTaskDependencyFromProjectDependency(true, it.name)

                    it.into('META-INF/bundled-libs') {
                        from bundledConf
                    }

                    it.from({
                        packedConf.incoming.artifactView {}.files.collect {
                            project.zipTree(it)
                        }
                    })

                    // configure entry point to the common application bootstrap
                    if (comp.isApplication()) {
                        it.manifest {
                            attributes(
                                    "Main-Class": "com.orbyfied.slate.bootstrap.ApplicationBootstrap"
                            )
                        }
                    }
                }
            }

            // configure jar tasks for duplicates? idk why this is needed
            project.tasks.withType(Jar).configureEach {
                it.duplicatesStrategy = DuplicatesStrategy.WARN
            }

            // add package metadata task
            var metadataTask = project.tasks.register("packageMetadata", GeneratePackageMetadataTask) {
                it.outputFile.set(project.layout.buildDirectory.file("generated/slate/package/slate.package.json"))
            }

            // add package bundle task
            def packageJarTask = project.tasks.register("packageJar", Jar) {
                it.archiveFileName.set("pk+" + packageDef.packageQualifier + "@" + packageDef.version + ".jar")

                it.dependsOn(metadataTask)
                from(metadataTask.get().outputFile)

                // copy component jars and metadata files
                for (def comp : packageDef.components) {
                    it.dependsOn(comp.jarTask)

                    from(comp.jarTask.flatMap { it.archiveFile }) {
                        rename { comp.embeddedJarName() }
                    }

                    from(comp.moduleMetadataTask.get().componentManifestFile) {
                        rename { "component." + comp.uniqueID + ".json" }
                    }
                }
            }

            project.tasks.named("build").configure {
                dependsOn(packageJarTask)
            }
        } }
    }
}