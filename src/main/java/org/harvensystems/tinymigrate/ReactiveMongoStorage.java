package org.harvensystems.tinymigrate;

import org.harvensystems.tinymigrate.api.ReactiveDistributedLockManager;
import org.harvensystems.tinymigrate.api.ReactiveMigrationStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(ReactiveMongoTemplate.class)
public class ReactiveMongoStorage implements ReactiveMigrationStorage, ReactiveDistributedLockManager {
    private static final String MIGRATIONS_COLLECTION = "migrations";
    private static final String MIGRATIONS_LOCK = "migrations_lock";

    final Logger logger = LoggerFactory.getLogger(ReactiveMongoStorage.class);
    final ReactiveMongoTemplate mongoTemplate;

    public ReactiveMongoStorage(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Mono<Boolean> isExecuted(String id) {
        return mongoTemplate.findOne(Query.query(Criteria.where("_id").is(id)), MigrationLog.class, MIGRATIONS_COLLECTION)
                .map(migrationLog -> migrationLog.getState() == MigrationLog.State.SUCCESS)
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Void> saveLog(MigrationLog log) {
        return mongoTemplate.save(log, MIGRATIONS_COLLECTION).then();
    }

    @Override
    public Mono<Boolean> acquireLock(String lockId, Duration expiration) {
        return mongoTemplate.insert(
                new HashMap<String, Object>(Map.of("_id", lockId, "lockedAt", Instant.now())),
                MIGRATIONS_LOCK
        )
                .map(result -> true)
                .onErrorResume(DuplicateKeyException.class, (ex) -> {
                    logger.warn("Waiting for lock held by another node");
                    return Mono.just(false);
                })
                .filter(result -> result)
                .switchIfEmpty(Mono.error(new RuntimeException("Failed to acquire lock")))
                .retryWhen(Retry.fixedDelay(20, Duration.ofSeconds(5)) )
                .onErrorResume(Exception.class, ex -> {
                    logger.error("Failed to acquire lock for {}", lockId, ex);
                    return Mono.error(new RuntimeException("Failed to acquire lock", ex));
                })
                .doOnNext(result -> logger.trace("Acquired lock for {}", lockId));
    }

    @Override
    public Mono<Void> releaseLock(String lockId) {
        return mongoTemplate.remove(Query.query(Criteria.where("_id").is(lockId)), MIGRATIONS_LOCK)
                .then();
    }
}
