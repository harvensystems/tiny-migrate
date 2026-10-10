package org.harvensystems.tinymigrate.reactive;

import reactor.core.publisher.Mono;

import java.time.Duration;

public interface ReactiveDistributedLockManager {
    Mono<Boolean> acquireLock(String lockId, Duration expiration);
    Mono<Void> releaseLock(String lockId);
}
