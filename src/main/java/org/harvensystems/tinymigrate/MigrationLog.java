package org.harvensystems.tinymigrate;

public class MigrationLog {
    private final String id;

    public MigrationLog(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
