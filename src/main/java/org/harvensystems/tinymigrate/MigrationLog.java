package org.harvensystems.tinymigrate;

import org.springframework.data.annotation.Id;

import java.time.Instant;

public class MigrationLog {
    private String id;
    private Instant createdAt;
    private State state;

    public MigrationLog() {}

    public static MigrationLog failure(String id) {
        return create(id, State.FAILURE);
    }

    public static MigrationLog success(String id) {
        return create(id, State.SUCCESS);
    }

    public static MigrationLog create(String id, State state) {
        MigrationLog log = new MigrationLog();
        log.id = id;
        log.createdAt = Instant.now();
        log.state = state;
        return log;
    }

    @Id
    public String getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public State getState() {
        return state;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setState(State state) {
        this.state = state;
    }

    public enum State {
        SUCCESS,
        FAILURE
    }
}
