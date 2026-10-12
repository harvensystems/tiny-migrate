package org.harvensystems.tinymigrate.config;

import org.harvensystems.tinymigrate.ReactiveMigrationRunner;
import org.harvensystems.tinymigrate.ReactiveMongoStorage;
import org.harvensystems.tinymigrate.api.MigrationStep;
import org.harvensystems.tinymigrate.api.ReactiveDistributedLockManager;
import org.harvensystems.tinymigrate.api.ReactiveMigrationStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.util.List;

@Configuration
@ConditionalOnClass(ReactiveMongoTemplate.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class TinyMigrateReactiveAutoConfiguration {

    @Bean
    public ReactiveMigrationRunner migrationRunner(
            List<MigrationStep> steps,
            ReactiveMigrationStorage storage,
            ReactiveDistributedLockManager manager) {

        return new ReactiveMigrationRunner(steps, storage, manager);
    }

    @Bean
    @ConditionalOnMissingBean(ReactiveMigrationStorage.class)
    public ReactiveMigrationStorage reactiveMongoStorage(ReactiveMongoTemplate template) {
        return new ReactiveMongoStorage(template);
    }
}
