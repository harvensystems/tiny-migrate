package org.harvensystems.tinymigrate;

public interface MigrationStorage {
    boolean isExecuted(String id);
    void markExecuted(MigrationLog log);
}
