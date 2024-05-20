package org.kiwiproject.curator;

import static java.util.Objects.isNull;
import static org.kiwiproject.base.KiwiStrings.f;

import lombok.extern.slf4j.Slf4j;

import org.apache.curator.framework.recipes.locks.InterProcessLock;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
public class CuratorLockHelper2 {

    public LockAcquisitionResult acquire(InterProcessLock lock, long time, TimeUnit unit) {
        var acquired = true;
        try {
            acquired = lock.acquire(time, unit);
        } catch (Exception e) {
            return new LockAcquisitionResult.Failure(e);
        }

        if (!acquired) {
            var msg = f("Failed to acquire lock; timed out after {} {}", time, unit);
            LOG.warn(msg);
            return new LockAcquisitionResult.Timeout(Duration.ofMillis(unit.toMillis(time)));
        }

        return new LockAcquisitionResult.Success();
    }

    public void releaseQuietly(InterProcessLock lock) {
        if (isNull(lock)) {
            return;
        }

        try {
            lock.release();
        } catch (Exception e) {
            // ignore
            LOG.warn("Unable to release lock {}", lock, e);
        }
    }

    public void releaseLockQuietlyIfHeld(InterProcessLock lock) {
        if (isNull(lock)) {
            return;
        }

        if (lock.isAcquiredInThisProcess()) {
            LOG.trace("Releasing lock [{}]", lock);
            releaseQuietly(lock);
        } else {
            LOG.trace("This process does not own lock [{}]. Nothing to do.", lock);
        }
    }

    public UseLockResult useLock(InterProcessLock lock, long time, TimeUnit unit, Runnable action) {
        try {
            var acquisitionResult = acquire(lock, time, unit);

            if (acquisitionResult instanceof LockAcquisitionResult.Success) {
                try {
                    action.run();
                    return new UseLockResult.Success();
                } catch (Exception e) {
                    return new UseLockResult.ActionFailure(e);
                }
            }

            var failureResult = LockAcquisitionFailureResult.fromLockAcquisitionResult(acquisitionResult);
            return new UseLockResult.LockAcquisitionFailure(failureResult);
        } finally {
            releaseLockQuietlyIfHeld(lock);
        }
    }

    public <R> WithLockResult<R> withLock(InterProcessLock lock, long time, TimeUnit unit, Supplier<R> supplier) {
        try {
            var acquisitionResult = acquire(lock, time, unit);

            if (acquisitionResult instanceof LockAcquisitionResult.Success) {
                try {
                    var result = supplier.get();
                    return new WithLockResult.Success<>(result);
                } catch (Exception e) {
                    return new WithLockResult.ActionFailure<>(e);
                }
            }

            var failureResult = LockAcquisitionFailureResult.fromLockAcquisitionResult(acquisitionResult);
            return new WithLockResult.LockAcquisitionFailure<>(failureResult);
        } finally {
            releaseLockQuietlyIfHeld(lock);
        }
    }
}
