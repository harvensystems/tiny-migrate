package org.harvensystems.tinymigrate.api;

import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public interface MigrationStep {
    String getId();
    Mono<Void> run();

    static MigrationStep create(String id, Supplier<Mono<Void>> supplier) {
        return new MigrationStep() {
            @Override
            public String getId() { return id; }

            @Override
            public Mono<Void> run() { return supplier.get(); }
        };
    }
}
