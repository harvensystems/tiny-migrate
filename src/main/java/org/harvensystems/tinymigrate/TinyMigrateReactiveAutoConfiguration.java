package org.harvensystems.tinymigrate;

import org.harvensystems.tinymigrate.reactive.ReactiveDistributedLockManager;
import org.harvensystems.tinymigrate.reactive.ReactiveMigrationRunner;
import org.harvensystems.tinymigrate.reactive.ReactiveMigrationStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class TinyMigrateReactiveAutoConfiguration {
    final Logger logger = LoggerFactory.getLogger(TinyMigrateReactiveAutoConfiguration.class);

    @Bean
    public ReactiveMigrationRunner migrationRunner(List<MigrationStep> steps, ReactiveMigrationStorage storage, ReactiveDistributedLockManager manager) {
        return new ReactiveMigrationRunner(steps, storage, manager);
    }
}
