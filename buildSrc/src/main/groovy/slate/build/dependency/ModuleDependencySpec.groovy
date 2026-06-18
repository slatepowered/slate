package slate.build.dependency

import org.gradle.api.Project

class ModuleDependencySpec {

    String group
    String name
    String version
    String specifier
    String component
    boolean provided = false

    Project projectRef

    String configurationName
    String scope
    Map<String, Object> properties = [:]

    ModuleDependencySpec() {

    }

    ModuleDependencySpec(String group, String name, String version, String specifier) {
        this.group = group
        this.name = name
        this.version = version
        this.specifier = specifier
    }

    ModuleDependencySpec(Project proj) {
        this.projectRef = proj
    }

    boolean isProjectRef() {
        this.projectRef != null
    }

    ModuleDependencySpec provided() {
        this.provided = true
        return this
    }

    ModuleDependencySpec bundle() {
        this.configurationName = "bundled"
        this.provided = true
        return this
    }

    ModuleDependencySpec packed() {
        this.configurationName = "packed"
        this.provided = true
        return this
    }

    ModuleDependencySpec component(String component) {
        this.component = component
        return this
    }

    ModuleDependencySpec configuration(String name) {
        this.configurationName = name
        return this
    }

    ModuleDependencySpec properties(Map<String, Object> a) {
        if (properties != null) properties.putAll(a);
        return this;
    }

}