package org.harvensystems.tinymigrate.api;

import org.harvensystems.tinymigrate.MigrationLog;
import reactor.core.publisher.Mono;

public interface ReactiveMigrationStorage {
    Mono<Boolean> isExecuted(String id);
    Mono<Void> saveLog(MigrationLog log);
}
