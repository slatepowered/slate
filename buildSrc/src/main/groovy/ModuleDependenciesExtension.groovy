import org.gradle.api.Project

class ModuleDependenciesExtension {

    final List<SlateDependencySpec> modules = []

    SlateDependencySpec dependency(SlateDependencySpec dep) {
        modules.add(dep);
        return dep;
    }

    SlateDependencySpec dependency(String notation, String configuration = null) {
        String[] split = notation.split(":");
        dependency([
                group: split[0],
                name: split[1],
                version: split.length > 2 ? split[2] : null,
                specifier: split.length > 3 ? split[3] : null
        ]).configuration(configuration)
    }

    SlateDependencySpec dependency(Map<String, Object> args, String configuration = null) {
        dependency(new SlateDependencySpec(args.remove("group") as String, args.remove("name") as String,
                args.remove("version") as String, args.remove("specifier") as String).configuration(configuration).properties(new LinkedHashMap<>(args)))
    }

    SlateDependencySpec dependency(Project proj, Map<String, Object> map = null, String configuration = null) {
        dependency(new SlateDependencySpec(proj).properties(map).configuration(configuration))
    }

}