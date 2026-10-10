package org.harvensystems.tinymigrate.reactive;

import org.harvensystems.tinymigrate.MigrationLog;
import reactor.core.publisher.Mono;

public interface ReactiveMigrationStorage {
    Mono<Boolean> isExecuted(String id);
    Mono<Void> markExecuted(MigrationLog log);
}
