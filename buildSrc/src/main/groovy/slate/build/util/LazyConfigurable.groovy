package slate.build.util

class LazyConfigurable<V> {

  public boolean ready = false;
  private final List<Closure> configClosures = new ArrayList<>();

  void ready() {
    this.ready = true
    for (Closure closure : configClosures) {
      closure.delegate = this;
      closure.resolveStrategy = Closure.DELEGATE_FIRST
      closure()
    }

    configClosures.clear()
  }

  V configure(Closure<?> closure) {
    if (ready) {
      closure.delegate = this;
      closure.resolveStrategy = Closure.DELEGATE_FIRST
      closure()
    } else {
      configClosures.add(closure)
    }

    return (V) this
  }

}
