package org.kiwiproject.curator;

import java.time.Duration;

public sealed interface LockAcquisitionResult {
    record Success() implements LockAcquisitionResult  {}
    record Failure(Exception cause) implements LockAcquisitionResult {}
    record Timeout(Duration period) implements LockAcquisitionResult {}
}
