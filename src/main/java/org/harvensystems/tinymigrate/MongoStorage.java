package org.harvensystems.tinymigrate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Duration;

@ConditionalOnBean(MongoTemplate.class)
public class MongoStorage implements MigrationStorage, DistributedLockManager {
    final Logger logger = LoggerFactory.getLogger(MongoStorage.class);
    final MongoTemplate mongoTemplate;

    public MongoStorage(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public boolean isExecuted(String id) {
        return mongoTemplate.exists(Query.query(Criteria.where("_id").is(id)), "migrations");
    }

    @Override
    public void markExecuted(MigrationLog log) {
    }

    @Override
    public boolean acquireLock(String lockId, Duration expiration) {
        return false;
    }

    @Override
    public void releaseLock(String lockId) {
    }
}
