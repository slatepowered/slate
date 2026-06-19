package slate.build.dependency

class ModuleDependenciesExtension implements ModuleDependencyConsumer {

  final List<ModuleDependencySpec> dependencies = []

  public List<ModuleDependencySpec> collectApplicable(String scope) {
    List<ModuleDependencySpec> dest = new ArrayList<>();
    for (def dep : dependencies) {
      if (dep.scope == null || dep.scope.equalsIgnoreCase(scope)) {
        dest.add(dep)
      }
    }

    return dest
  }

  ModuleDependencySpec dependency(ModuleDependencySpec dep) {
    dependencies.add(dep);
    return dep;
  }

  static class Scoped implements ModuleDependencyConsumer {
    private final ModuleDependencyConsumer parent;
    private String scope;

    Scoped(ModuleDependencyConsumer parent) {
      this.parent = parent;
    }

    Scoped scope(String scope) {
      this.scope = scope
      return this
    }

    @Override
    ModuleDependencySpec dependency(ModuleDependencySpec spec) {
      spec.scope = scope
      return parent.dependency(spec)
    }
  }

}