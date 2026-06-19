package slate.build

import org.gradle.api.Project
import org.gradle.api.logging.Logger

class PluginLogger {

  private final Logger delegate
  private final String prefix

  PluginLogger(Project project, Class pluginClass) {
    delegate = project.logger
    prefix = "[${pluginClass.simpleName}]"
    if (project != null) {
      prefix += " :" + project.name + " —";
    }
  }

  void lifecycle(String msg) {
    delegate.lifecycle("${prefix} ${msg}")
  }

  void info(String msg) {
    delegate.info("${prefix} ${msg}")
  }

  void debug(String msg) {
    delegate.debug("${prefix} ${msg}")
  }

  void warn(String msg) {
    delegate.warn("${prefix} ${msg}")
  }
}
