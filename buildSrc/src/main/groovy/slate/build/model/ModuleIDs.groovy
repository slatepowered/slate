package slate.build.model

import java.security.MessageDigest;

class ModuleIDs {

  static String hashToID(String hash) {
    return hash.substring(0, 20)
  }

  static String hashUntrimmed(String qual) {
    return MessageDigest.getInstance("SHA-256")
        .digest(qual.bytes)
        .encodeHex()
        .toString()
  }

  static String hashID(String qual) {
    return hashToID(hashUntrimmed(qual))
  }

  static String qualifierFromDependency(String group, String name, String component = null) {
    return group + "." + name + (component != null ? "+" + component : "")
  }

}
