package slate.build.dependency

import org.gradle.api.Project
import slate.build.dependency.ModuleDependenciesExtension.Scoped

interface ModuleDependencyConsumer {

  ModuleDependencySpec dependency(ModuleDependencySpec spec);

  default Scoped scoped(String scope) {
    return new Scoped(this).scope(scope);
  }

  default Scoped scoped(String scope, @DelegatesTo(Scoped) Closure<?> closure) {
    Scoped scoped = scoped(scope);
    closure.delegate = scoped
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure(scoped);
    return scoped;
  }

  default ModuleDependencySpec dependency(String notation, String configuration = null) {
    String[] split = notation.split(":");
    dependency([
        group    : split[0],
        name     : split[1],
        version  : split.length > 2 ? split[2] : null,
        specifier: split.length > 3 ? split[3] : null
    ]).configuration(configuration)
  }

  default ModuleDependencySpec dependency(Map<String, Object> args, String configuration = null) {
    dependency(new ModuleDependencySpec(args.remove("group") as String, args.remove("name") as String,
        args.remove("version") as String, args.remove("specifier") as String).configuration(configuration).properties(new LinkedHashMap<>(args)))
  }

  default ModuleDependencySpec dependency(Project proj, Map<String, Object> map = null, String configuration = null) {
    dependency(new ModuleDependencySpec(proj).properties(map).configuration(configuration))
  }

}