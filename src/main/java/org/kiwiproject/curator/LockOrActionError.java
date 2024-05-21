package org.kiwiproject.curator;

public sealed interface LockOrActionError {
    record ActionFailure(Exception cause) implements LockOrActionError {}
    record LockAcquisitionFailure(LockAcquisitionFailureResult acquisitionResult) implements LockOrActionError {}
}
