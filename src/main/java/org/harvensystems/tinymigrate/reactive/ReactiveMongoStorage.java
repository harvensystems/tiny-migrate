package org.harvensystems.tinymigrate.reactive;

import com.mongodb.DuplicateKeyException;
import org.bson.Document;
import org.harvensystems.tinymigrate.MigrationLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.HashIndexed;
import org.springframework.data.mongodb.core.index.HashedIndex;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(ReactiveMongoTemplate.class)
public class ReactiveMongoStorage implements ReactiveMigrationStorage, ReactiveDistributedLockManager {
    private static final String MIGRATIONS_COLLECTION = "migrations";
    private static final Duration LOCK_TIMEOUT = Duration.ofMinutes(10);
    private static final String LOCK_COLLECTION = "migrations_locks";

    final Logger logger = LoggerFactory.getLogger(ReactiveMongoStorage.class);
    final ReactiveMongoTemplate mongoTemplate;

    public ReactiveMongoStorage(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Mono<Boolean> isExecuted(String id) {
        return mongoTemplate.exists(Query.query(Criteria.where("_id").is(id)), MIGRATIONS_COLLECTION)
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Void> markExecuted(MigrationLog log) {
        return mongoTemplate.save(log, MIGRATIONS_COLLECTION).then();
    }

    @Override
    public Mono<Boolean> acquireLock(String lockId, Duration expiration) {
        return
                ensureTtlIndex().then(
                    mongoTemplate.insert(
                    Map.of("lockId", lockId, "createdAt", Instant.now()),
                    LOCK_COLLECTION
            )
                    .map(o -> true)
                    .onErrorReturn(DuplicateKeyException.class, false)
                );
    }

    @Override
    public Mono<Void> releaseLock(String lockId) {
        return mongoTemplate.remove(Query.query(Criteria.where("lockId").is(lockId)), LOCK_COLLECTION).then();
    }

    private Mono<Void> ensureTtlIndex() {
        Document index = new Document("createdAt", 1).append("expireAfterSeconds", LOCK_TIMEOUT.getSeconds());
        Document command = new Document("createIndexes", LOCK_COLLECTION).append("indexes", List.of(index));
        return mongoTemplate.executeCommand(command).then();
    }
}
