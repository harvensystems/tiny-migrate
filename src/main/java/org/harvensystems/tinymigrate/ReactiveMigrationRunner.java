package org.harvensystems.tinymigrate;

import org.harvensystems.tinymigrate.api.MigrationStep;
import org.harvensystems.tinymigrate.api.ReactiveDistributedLockManager;
import org.harvensystems.tinymigrate.api.ReactiveMigrationStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

public class ReactiveMigrationRunner implements ApplicationRunner {

    private static final String LOCK_NAME = "migration";

    private final Logger logger = LoggerFactory.getLogger(ReactiveMigrationRunner.class);

    private final List<MigrationStep> steps;
    private final ReactiveMigrationStorage storage;
    private final ReactiveDistributedLockManager lockManager;

    public ReactiveMigrationRunner(
            List<MigrationStep> steps,
            ReactiveMigrationStorage storage,
            ReactiveDistributedLockManager lockManager) {

        this.steps = steps;
        this.storage = storage;
        this.lockManager = lockManager;
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.debug("Running migration steps");
        lockManager.acquireLock(LOCK_NAME, Duration.ofMinutes(15))
                .filter(acquired -> acquired)
                .flatMapIterable(acquired -> steps)
                .sort(Comparator.comparing(MigrationStep::getId))
                .concatMap(step -> {
                    return storage.isExecuted(step.getId())
                            .flatMap(res -> {
                                if(res) {
                                    logger.info("Migration step {} already executed", step.getId());
                                    return Mono.empty();
                                }
                                logger.info("Executing migration step {}", step.getId());
                                return step.run()
                                        .then(storage.saveLog(MigrationLog.success(step.getId())))
                                        .onErrorResume(throwable -> {
                                            logger.error("Error executing migration step {}", step.getId(), throwable);
                                            return storage.saveLog(MigrationLog.failure(step.getId()))
                                                    .then(Mono.error(new RuntimeException(throwable)));

                                        });
                            });
                })
                .onErrorResume(throwable -> {
                    logger.error("Error executing migration");
                    return lockManager.releaseLock(LOCK_NAME).then(Mono.error(throwable));
                })
                .then(Mono.just(true))
                .delayUntil(v -> lockManager.releaseLock(LOCK_NAME))
                .block(Duration.ofMinutes(15));
    }
}
