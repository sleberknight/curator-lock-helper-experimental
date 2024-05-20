package org.kiwiproject.curator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.kiwiproject.test.assertj.KiwiAssertJ.assertIsExactType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

@DisplayName("LockAcquisitionFailureResult")
class LockAcquisitionFailureResultTest {

    @Nested
    class FromLockAcquisitionResult {

        @Test
        void shouldThrowIllegalArgumentException_ForNullArgument() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> LockAcquisitionFailureResult.fromLockAcquisitionResult(null));
        }

        @Test
        void shouldNotAllowLockAcquisitionSuccess() {
            var acquisitionResult = new LockAcquisitionResult.Success();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> LockAcquisitionFailureResult.fromLockAcquisitionResult(acquisitionResult));
        }

        @Test
        void shouldReturnFailure_FromLockAcquisitionFailure() {
            var cause = new RuntimeException();
            var acquisitionResult = new LockAcquisitionResult.Failure(cause);
            var result = LockAcquisitionFailureResult.fromLockAcquisitionResult(acquisitionResult);

            var failureResult = assertIsExactType(result, LockAcquisitionFailureResult.Failure.class);

            assertThat(failureResult.cause()).isSameAs(cause);
        }

        @Test
        void shouldReturnTimeout_FromLockAcquisitionTimeout() {
            var period = Duration.ofSeconds(3L);
            var acquisitionResult = new LockAcquisitionResult.Timeout(period);

            var result = LockAcquisitionFailureResult.fromLockAcquisitionResult(acquisitionResult);

            var timeoutResult = assertIsExactType(result, LockAcquisitionFailureResult.Timeout.class);

            assertThat(timeoutResult.period()).isEqualTo(period);
        }
    }
}
