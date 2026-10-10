package org.harvensystems.tinymigrate;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public interface MigrationAction {

    void run();

    /**
     * Creates a {@link MigrationAction} from a given {@link Runnable}.
     * The returned {@link MigrationAction} executes the given {@link Runnable}'s
     * {@code run} method when invoked.
     *
     * @param runnable the {@link Runnable} to be encapsulated by the {@link MigrationAction};
     *                 must not be {@code null}.
     * @return a {@link MigrationAction} that executes the given {@link Runnable} when run.
     */
    static MigrationAction fromRunnable(Runnable runnable) {
        return runnable::run;
    }

    /**
     * Creates a {@link MigrationAction} from a given {@link Supplier} of a {@link Mono} or {@link Flux}.
     * The returned {@link MigrationAction} executes the given {@link Supplier}'s
     * {@code get} method and blocks until the returned {@link Mono} or {@link Flux} completes.
     *
     * @param supplier the {@link Supplier} of a {@link Mono} or {@link Flux} to be encapsulated by the {@link MigrationAction};
     *                 must not be {@code null}.
     * @return a {@link MigrationAction} that executes the given {@link Supplier} when run.
     */
    static MigrationAction fromPublisher(Supplier<?> supplier) {
        return () -> {
            Object publisher = supplier.get();
            if (publisher instanceof Mono) {
                ((Mono<?>) publisher).block();
            } else if (publisher instanceof Flux) {
                ((Flux<?>) publisher).blockLast();
            } else {
                throw new IllegalArgumentException("Unsupported publisher type: " + publisher.getClass().getName());
            }
        };
    }
}
