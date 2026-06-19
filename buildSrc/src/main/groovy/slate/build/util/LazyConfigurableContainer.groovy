package slate.build.util

class LazyConfigurableContainer<V> {

  static <V> LazyConfigurableContainer<V> create() {
    return new LazyConfigurableContainer<V>();
  }

  List<Closure> laterConfigs = new ArrayList<>()
  V instance = null

  void set(V instance) {
    this.instance = instance
    for (Closure closure : laterConfigs) {
      closure.delegate = instance
      closure.resolveStrategy = Closure.DELEGATE_FIRST
      closure()
    }

    laterConfigs.clear()
  }

  V get() {
    return this.instance
  }

  void configure(Closure<?> closure) {
    if (instance == null) laterConfigs.add(closure);
    else {
      closure.delegate = instance
      closure.resolveStrategy = Closure.DELEGATE_FIRST
      closure()
    }
  }

}
