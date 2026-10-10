package org.harvensystems.tinymigrate.reactive;

import org.harvensystems.tinymigrate.MigrationLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

import java.time.Duration;

@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(ReactiveMongoTemplate.class)
public class ReactiveMongoStorage implements ReactiveMigrationStorage, ReactiveDistributedLockManager {
    final Logger logger = LoggerFactory.getLogger(ReactiveMongoStorage.class);
    final ReactiveMongoTemplate mongoTemplate;

    public ReactiveMongoStorage(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Mono<Boolean> isExecuted(String id) {
        return mongoTemplate.exists(Query.query(Criteria.where("_id").is(id)), "migrations")
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Void> markExecuted(MigrationLog log) {
        return Mono.empty();
    }

    @Override
    public Mono<Boolean> acquireLock(String lockId, Duration expiration) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> releaseLock(String lockId) {
        return Mono.empty();
    }
}
