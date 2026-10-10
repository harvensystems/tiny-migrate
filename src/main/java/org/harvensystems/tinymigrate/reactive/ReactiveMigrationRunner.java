package org.harvensystems.tinymigrate.reactive;

import org.harvensystems.tinymigrate.DistributedLockManager;
import org.harvensystems.tinymigrate.MigrationLog;
import org.harvensystems.tinymigrate.MigrationStep;
import org.harvensystems.tinymigrate.MigrationStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

public class ReactiveMigrationRunner implements ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(ReactiveMigrationRunner.class);

    private final List<MigrationStep> steps;
    private final ReactiveMigrationStorage storage;
    private final ReactiveDistributedLockManager lockManager;

    private final String lockName = "migration";

    public ReactiveMigrationRunner(List<MigrationStep> steps, ReactiveMigrationStorage storage, ReactiveDistributedLockManager lockManager) {
        this.steps = steps;
        this.storage = storage;
        this.lockManager = lockManager;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.debug("Running migration steps");
        lockManager.acquireLock(lockName, Duration.ofMinutes(15))
                .filter(acquired -> acquired)
                .flatMapIterable(acquired -> steps)
                .concatMap(step -> {
                    step.getAction().run();
                    return Mono.just(true);
                })
                .blockLast();
//        if (!acquired) {
//            return;
//        }
//
//        for (MigrationStep step : steps) {
//            if (storage.isExecuted(step.getId())) {
//                logger.debug("Migration step {} already executed, skipping", step.getId());
//                continue;
//            }
//
//            logger.debug("Executing migration step {}", step.getId());
//            step.getAction().run();
//
//            logger.debug("Marking migration step {} as executed", step.getId());
//            storage.markExecuted(new MigrationLog(step.getId()));
//        }
//
//        lockManager.releaseLock(lockName);
//        logger.debug("Migration steps completed");
    }
}
