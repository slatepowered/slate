package slate.build.packages


import slate.build.component.PackageComponent

class PackageExtension {

  String version;

  // Full package name, "{group}.{name}" by default
  String packageQualifier;

  // Package IDs
  String uniqueID;
  String qualifierHash;
  String trimmedQualifierHash;

  // Whether the package may contain compilable code
  boolean code = true;

  // Package Components
  Map<String, PackageComponent> components = new HashMap<>()
  Closure<?> allConfig

  Collection<PackageComponent> components() {
    return components.values()
  }

  PackageComponent component(String name) {
    def comp = components.get(name)
    if (comp != null) {
      return comp
    }

    comp = new PackageComponent();
    comp.name = name;
    components.put(name, comp);
    return comp;
  }

  PackageComponent component(String name, @DelegatesTo(PackageComponent) Closure<?> conf) {
    PackageComponent c = component(name);
    c.configure(conf)
    return c;
  }

  PackageComponent mainComponent() {
    return component("main")
  }

  PackageComponent mainComponent(@DelegatesTo(PackageComponent) Closure<?> conf) {
    return component("main", conf)
  }

  void forAll(@DelegatesTo(PackageComponent) Closure<?> conf) {
    allConfig = conf
  }

  void readyComponents() {
    for (PackageComponent comp : components.values()) {
      if (allConfig != null) {
        allConfig.delegate = comp
        allConfig.resolveStrategy = Closure.DELEGATE_FIRST
        allConfig()
      }

      comp.ready()
    }
  }

}
