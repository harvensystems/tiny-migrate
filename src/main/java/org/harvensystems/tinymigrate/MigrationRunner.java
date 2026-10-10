package org.harvensystems.tinymigrate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.time.Duration;
import java.util.List;

public class MigrationRunner implements ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(MigrationRunner.class);

    private final List<MigrationStep> steps;
    private final MigrationStorage storage;
    private final DistributedLockManager lockManager;

    private final String lockName = "migration";

    public MigrationRunner(List<MigrationStep> steps, MigrationStorage storage, DistributedLockManager lockManager) {
        this.steps = steps;
        this.storage = storage;
        this.lockManager = lockManager;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.debug("Running migration steps");
        boolean acquired = lockManager.acquireLock(lockName, Duration.ofMinutes(15));
        if (!acquired) {
            return;
        }

        for (MigrationStep step : steps) {
            if (storage.isExecuted(step.getId())) {
                logger.debug("Migration step {} already executed, skipping", step.getId());
                continue;
            }

            logger.debug("Executing migration step {}", step.getId());
            step.getAction().run();

            logger.debug("Marking migration step {} as executed", step.getId());
            storage.markExecuted(new MigrationLog(step.getId()));
        }

        lockManager.releaseLock(lockName);
        logger.debug("Migration steps completed");
    }
}
