package org.harvensystems.tinymigrate;

public interface MigrationStep {
    String getId();
    MigrationAction getAction();
}
