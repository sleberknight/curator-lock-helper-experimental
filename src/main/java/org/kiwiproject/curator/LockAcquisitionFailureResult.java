package org.kiwiproject.curator;

import static com.google.common.base.Preconditions.checkState;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import java.time.Duration;

public sealed interface LockAcquisitionFailureResult {

    record Failure(Exception cause) implements LockAcquisitionFailureResult {}
    record Timeout(Duration period) implements LockAcquisitionFailureResult {}

    public static LockAcquisitionFailureResult fromLockAcquisitionResult(LockAcquisitionResult acquisitionResult) {
        checkArgumentNotNull(acquisitionResult, "acquisitionResult must not be null");

        // 'pattern matching for switch' is not until 21 !
        // return switch(acquisitionResult) {
        //     case LockAcquisitionResult.Success s -> throw new IllegalArgumentException("LockAcquisitionResult must not be Success");
        //     case LockAcquisitionResult.Failure f -> new LockAcquisitionFailureResult.Failure(f.cause());
        //     case LockAcquisitionResult.Timeout t -> new LockAcquisitionFailureResult.Timeout(t.period());
        // };

        if (acquisitionResult instanceof LockAcquisitionResult.Failure f) {
            return new Failure(f.cause());
        }

        if (acquisitionResult instanceof LockAcquisitionResult.Timeout t) {
            return new Timeout(t.period());
        }

        var unsupportedResultType = acquisitionResult.getClass();
        checkState(acquisitionResult instanceof LockAcquisitionResult.Success,
                "acquisitionResult is unknown (unsupported) type: %s", unsupportedResultType.getName());

        throw new IllegalArgumentException("LockAcquisitionResult must not be " + unsupportedResultType.getSimpleName());
    }
}
