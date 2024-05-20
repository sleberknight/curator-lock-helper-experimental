package org.kiwiproject.curator;

public sealed interface UseLockResult {
    record Success() implements UseLockResult {}
    record ActionFailure(Exception cause) implements UseLockResult {}
    record LockAcquisitionFailure(LockAcquisitionFailureResult acquisitionResult) implements UseLockResult {}
}
