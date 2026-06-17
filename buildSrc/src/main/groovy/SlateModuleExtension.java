public class SlateModuleExtension {

  String uniqueID;
  String qualifierHash;
  String trimmedQualifierHash;

  String entrypoint;

  boolean isApplication() {
    return entrypoint != null;
  }

}
