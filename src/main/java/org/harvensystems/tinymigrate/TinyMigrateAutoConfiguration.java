package org.harvensystems.tinymigrate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.List;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class TinyMigrateAutoConfiguration {
    final Logger logger = LoggerFactory.getLogger(TinyMigrateAutoConfiguration.class);

    @Bean
    public MigrationRunner migrationRunner(List<MigrationStep> steps, MigrationStorage storage, DistributedLockManager manager) {

        System.out.println("Initializing migration runner");
        return new MigrationRunner(steps, storage, manager);
    }

//    @Bean
//    public MigrationStorage migrationStorage() {
//        return new MigrationStorage() {
//            @Override
//            public boolean isExecuted(String id) {
//                System.out.println("Checking if migration step " + id + " is executed");
//                return false;
//            }
//
//            @Override
//            public void markExecuted(MigrationLog log) {
//                System.out.println("Marking migration step " + log.getId() + " as executed");
//            }
//        };
//    }
//
//    @Bean
//    public DistributedLockManager distributedLockManager() {
//        return new DistributedLockManager() {
//            @Override
//            public boolean acquireLock(String lockId, Duration expiration) {
//                System.out.println("Acquiring lock for migration step " + lockId);
//                return true;
//            }
//
//            @Override
//            public void releaseLock(String lockId) {
//                System.out.println("Releasing lock for migration step " + lockId);
//            }
//        };
//    }
}
