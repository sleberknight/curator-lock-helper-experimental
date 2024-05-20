package org.kiwiproject.curator;

public sealed interface WithLockResult<R> {
    record Success<R>(R result) implements WithLockResult<R> {}
    record ActionFailure<R>(Exception cause) implements WithLockResult<R> {}
    record LockAcquisitionFailure<R>(LockAcquisitionFailureResult acquisitionResult) implements WithLockResult<R> {}
}
