import org.gradle.api.Project

class SlateDependencySpec {

    String group
    String name
    String version
    String specifier

    Project projectRef

    String configurationName;
    Map<String, Object> properties = [:]

    SlateDependencySpec() {

    }

    SlateDependencySpec(String group, String name, String version, String specifier) {
        this.group = group
        this.name = name
        this.version = version
        this.specifier = specifier
    }

    SlateDependencySpec(Project proj) {
        this.projectRef = proj
    }

    boolean isProjectRef() {
        this.projectRef != null
    }

    SlateDependencySpec configuration(String name) {
        this.configurationName = name
        return this
    }

    SlateDependencySpec properties(Map<String, Object> a) {
        if (properties != null) properties.putAll(a);
        return this;
    }

}