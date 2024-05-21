package org.kiwiproject.curator;

public sealed interface UseLockError {
    record ActionFailure(Exception cause) implements UseLockError {}
    record LockAcquisitionFailure(LockAcquisitionFailureResult acquisitionResult) implements UseLockError {}
}
