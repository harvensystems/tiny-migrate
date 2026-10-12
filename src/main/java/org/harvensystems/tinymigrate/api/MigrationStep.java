package org.harvensystems.tinymigrate.api;

import reactor.core.publisher.Mono;

public interface MigrationStep {
    String getId();
    Mono<Void> run();
}
