package slate.build

import org.gradle.api.Project
import org.gradle.api.provider.Property;
import slate.build.module.PackageComponent

class PackageExtension {

  String version;

  // Full package name, "{group}.{name}" by default
  String packageQualifier;

  // Package IDs
  String uniqueID;
  String qualifierHash;
  String trimmedQualifierHash;

  // Package Components
  List<PackageComponent> components = new ArrayList<>();

  PackageComponent component(String name) {
    def comp = components.find { it.name.equalsIgnoreCase(name) }
    if (comp != null) {
      return comp
    }

    comp = new PackageComponent();
    comp.name = name;
    components.add(comp)
    return comp;
  }

  PackageComponent component(String name, @DelegatesTo(PackageComponent) Closure<?> conf) {
    PackageComponent c = component(name);
    conf.delegate = c
    conf.resolveStrategy = Closure.DELEGATE_FIRST;
    conf(c);
    return c;
  }

  PackageComponent mainComponent() {
    return component("main")
  }

  PackageComponent mainComponent(@DelegatesTo(PackageComponent) Closure<?> conf) {
    return component("main", conf)
  }

}
