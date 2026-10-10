package org.harvensystems.tinymigrate;

import java.time.Duration;

public interface DistributedLockManager {
    boolean acquireLock(String lockId, Duration expiration);
    void releaseLock(String lockId);
}
