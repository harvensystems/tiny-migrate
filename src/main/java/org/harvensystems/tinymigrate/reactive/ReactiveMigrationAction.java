package org.harvensystems.tinymigrate.reactive;

import reactor.core.publisher.Mono;

public interface ReactiveMigrationAction {

    Mono<Void> run();

//    /**
//     * Creates a {@link ReactiveMigrationAction} from a given {@link Runnable}.
//     * The returned {@link ReactiveMigrationAction} executes the given {@link Runnable}'s
//     * {@code run} method when invoked.
//     *
//     * @param runnable the {@link Runnable} to be encapsulated by the {@link ReactiveMigrationAction};
//     *                 must not be {@code null}.
//     * @return a {@link ReactiveMigrationAction} that executes the given {@link Runnable} when run.
//     */
//    static ReactiveMigrationAction fromRunnable(Runnable runnable) {
//        return Mono.fromRunnable(runnable);
//    }
//
//    /**
//     * Creates a {@link ReactiveMigrationAction} from a given {@link Supplier} of a {@link Mono} or {@link Flux}.
//     * The returned {@link ReactiveMigrationAction} executes the given {@link Supplier}'s
//     * {@code get} method and blocks until the returned {@link Mono} or {@link Flux} completes.
//     *
//     * @param supplier the {@link Supplier} of a {@link Mono} or {@link Flux} to be encapsulated by the {@link ReactiveMigrationAction};
//     *                 must not be {@code null}.
//     * @return a {@link ReactiveMigrationAction} that executes the given {@link Supplier} when run.
//     */
//    static ReactiveMigrationAction fromPublisher(Supplier<?> supplier) {
//        return () -> {
//            Object publisher = supplier.get();
//            if (publisher instanceof Mono) {
//                ((Mono<?>) publisher);
//            } else if (publisher instanceof Flux) {
//                ((Flux<?>) publisher);
//            } else {
//                throw new IllegalArgumentException("Unsupported publisher type: " + publisher.getClass().getName());
//            }
//        };
//    }
}
