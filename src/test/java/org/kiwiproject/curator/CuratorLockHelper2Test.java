package org.kiwiproject.curator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.kiwiproject.test.assertj.KiwiAssertJ.assertIsExactType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import lombok.Getter;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@DisplayName("CuratorLockHelper2")
class CuratorLockHelper2Test {

    private CuratorLockHelper2 lockHelper;
    private InterProcessMutex lock;

    @BeforeEach
    void setUp() {
        lock = mock(InterProcessMutex.class);
        lockHelper = new CuratorLockHelper2();
    }

    @Nested
    class Acquire {

        @Test
        void shouldReturnSuccess_WhenAcquiresLock() throws Exception {
            when(lock.acquire(10, TimeUnit.SECONDS)).thenReturn(true);

            var lockAcquisitionResult = lockHelper.acquire(lock, 10, TimeUnit.SECONDS);
            assertThat(lockAcquisitionResult).isExactlyInstanceOf(LockAcquisitionResult.Success.class);

            verify(lock, only()).acquire(10, TimeUnit.SECONDS);
        }

        @Test
        void shouldReturnLockAcquisitionFailure_WhenFails_DueToException() throws Exception {
            doThrow(new CannotAcquireLockException("oops, can't touch this lock")).when(lock).acquire(10, TimeUnit.SECONDS);

            var lockAcquisitionResult = lockHelper.acquire(lock, 10, TimeUnit.SECONDS);
            var failureResult = assertIsExactType(lockAcquisitionResult, LockAcquisitionResult.Failure.class);

            assertThat(failureResult.cause())
                    .isInstanceOf(CannotAcquireLockException.class)
                    .hasMessage("oops, can't touch this lock");

            verify(lock, only()).acquire(10, TimeUnit.SECONDS);
        }

        @Test
        void shouldReturnLockAcquisitionTimeoutException_WhenFails_DueToTimeout() throws Exception {
            when(lock.acquire(10, TimeUnit.SECONDS)).thenReturn(false);

            var lockAcquisitionResult = lockHelper.acquire(lock, 10, TimeUnit.SECONDS);
            var timeoutResult = assertIsExactType(lockAcquisitionResult, LockAcquisitionResult.Timeout.class);

            assertThat(timeoutResult.period()).isEqualTo(Duration.ofSeconds(10));

            verify(lock, only()).acquire(10, TimeUnit.SECONDS);
        }
    }

    @Nested
    class ReleaseQuietly {

        @Test
        void shouldIgnore_NullLock() {
            assertThatCode(() -> lockHelper.releaseQuietly(null))
                    .doesNotThrowAnyException();
        }

        @Test
        void shouldUnlock_WhenSuccessfullyReleases() throws Exception {
            assertThatCode(() -> lockHelper.releaseQuietly(lock))
                    .doesNotThrowAnyException();

            verify(lock, only()).release();
        }

        @Test
        void shouldNotThrow_WhenFailsToRelease() throws Exception {
            doThrow(new Exception("crapola")).when(lock).release();

            assertThatCode(() -> lockHelper.releaseQuietly(lock))
                    .doesNotThrowAnyException();

            verify(lock, only()).release();
        }
    }

    @Nested
    class ReleaseLockQuietlyIfHeld {

        @Test
        void shouldIgnore_NullLock() {
            assertThatCode(() -> lockHelper.releaseLockQuietlyIfHeld(null))
                    .doesNotThrowAnyException();
        }

        @Test
        void shouldRelease_WhenWeOwnTheLock() throws Exception {
            when(lock.isAcquiredInThisProcess()).thenReturn(true);

            lockHelper.releaseLockQuietlyIfHeld(lock);

            verify(lock).isAcquiredInThisProcess();
            verify(lock).release();
            verifyNoMoreInteractions(lock);
        }

        @Test
        void shouldNotRelease_WhenWeDoNotOwnTheLock() throws Exception {
            when(lock.isAcquiredInThisProcess()).thenReturn(false);

            lockHelper.releaseLockQuietlyIfHeld(lock);

            verify(lock, never()).release();
        }
    }

    private static class CannotAcquireLockException extends RuntimeException {
        CannotAcquireLockException(String message) {
            super(message);
        }
    }

    @Nested
    class UseLock {

        @Test
        void shouldCallRunnable_WhenAcquiresLock() throws Exception {
            when(lock.acquire(anyLong(), any(TimeUnit.class))).thenReturn(true);
            when(lock.isAcquiredInThisProcess()).thenReturn(true);

            var action = new TrackingRunnable();
            var result = lockHelper.useLock(lock, 3, TimeUnit.SECONDS, action);
            assertThat(result).isInstanceOf(UseLockResult.Success.class);
            assertThat(action.wasCalled).isTrue();

            verify(lock).acquire(3, TimeUnit.SECONDS);
            verify(lock).isAcquiredInThisProcess();
            verify(lock).release();
            verifyNoMoreInteractions(lock);
        }

        @Test
        void shouldReturnLockAcquisitionFailure_WhenFails_DueToException() throws Exception {
            doThrow(new CannotAcquireLockException("oops, can't touch this lock"))
                    .when(lock)
                    .acquire(anyLong(), any(TimeUnit.class));

            var action = new TrackingRunnable();
            var result = lockHelper.useLock(lock, 2, TimeUnit.SECONDS, action);

            var lockAcquisitionFailure = assertIsExactType(result, UseLockResult.LockAcquisitionFailure.class);
            assertThat(lockAcquisitionFailure.acquisitionResult())
                    .isInstanceOf(LockAcquisitionFailureResult.Failure.class);
            assertThat(action.wasCalled).isFalse();

            verify(lock).acquire(2, TimeUnit.SECONDS);
            verify(lock).isAcquiredInThisProcess();
            verifyNoMoreInteractions(lock);
        }

        @Test
        void shouldReturnLockAcquisitionTimeout_WhenFails_DueToTimeout() throws Exception {
            when(lock.acquire(anyLong(), any(TimeUnit.class))).thenReturn(false);
            when(lock.isAcquiredInThisProcess()).thenReturn(false);

            var action = new TrackingRunnable();
            var result = lockHelper.useLock(lock, 1, TimeUnit.SECONDS, action);

            var lockAcquisitionFailure = assertIsExactType(result, UseLockResult.LockAcquisitionFailure.class);
            assertThat(lockAcquisitionFailure.acquisitionResult())
                    .isInstanceOf(LockAcquisitionFailureResult.Timeout.class);
            assertThat(action.wasCalled).isFalse();

            verify(lock).acquire(1, TimeUnit.SECONDS);
            verify(lock).isAcquiredInThisProcess();
            verifyNoMoreInteractions(lock);
        }

        @Test
        void shouldReleaseLock_IfActionThrowsAnException() throws Exception {
            when(lock.acquire(anyLong(), any(TimeUnit.class))).thenReturn(true);
            when(lock.isAcquiredInThisProcess()).thenReturn(true);

            var action = new ThrowingRunnable();
            var result = lockHelper.useLock(lock, 1, TimeUnit.SECONDS, action);

            var lockAcquisitionFailure = assertIsExactType(result, UseLockResult.ActionFailure.class);
            assertThat(lockAcquisitionFailure.cause())
                    .isInstanceOf(UncheckedIOException.class)
                    .hasMessageContaining("I/O error")
                    .cause()
                    .isInstanceOf(IOException.class)
                    .hasMessage("I/O error");

            verify(lock).acquire(1, TimeUnit.SECONDS);
            verify(lock).isAcquiredInThisProcess();
            verify(lock).release();
            verifyNoMoreInteractions(lock);
        }
    }

    @Nested
    class WithLock {

        @Test
        void shouldReturnResultOfSupplier_WhenAcquiresLock() throws Exception {
            when(lock.acquire(anyLong(), any(TimeUnit.class))).thenReturn(true);
            when(lock.isAcquiredInThisProcess()).thenReturn(true);

            var number = 84L;
            var supplier = new TrackingSupplier(number);
            var result = lockHelper.withLock(lock, 2, TimeUnit.SECONDS, supplier);

            var successResult = assertIsExactType(result, WithLockResult.Success.class);
            assertThat(successResult.result()).isEqualTo(number);
            assertThat(supplier.wasCalled).isTrue();

            verify(lock).acquire(2, TimeUnit.SECONDS);
            verify(lock).isAcquiredInThisProcess();
            verify(lock).release();
            verifyNoMoreInteractions(lock);
        }

        @Test
        void shouldReturnLockAcquisitionFailureException_WhenFails_DueToException() throws Exception {
            doThrow(new CannotAcquireLockException("oops, can't touch this lock"))
                    .when(lock)
                    .acquire(anyLong(), any(TimeUnit.class));
            when(lock.isAcquiredInThisProcess()).thenReturn(false);

            var supplier = new TrackingSupplier();
            var result = lockHelper.withLock(lock, 5, TimeUnit.SECONDS, supplier);

            var lockAcquisitionFailure = assertIsExactType(result, WithLockResult.LockAcquisitionFailure.class);

            var failureResult = assertIsExactType(lockAcquisitionFailure.acquisitionResult(), LockAcquisitionFailureResult.Failure.class);
            assertThat(failureResult.cause()).isInstanceOf(CannotAcquireLockException.class);

            assertThat(supplier.wasCalled).isFalse();

            verify(lock).acquire(5, TimeUnit.SECONDS);
            verify(lock).isAcquiredInThisProcess();
            verifyNoMoreInteractions(lock);
        }

        @Test
        void shouldReturnLockAcquisitionTimeoutException_WhenFails_DueToTimeout() throws Exception {
            when(lock.acquire(anyLong(), any(TimeUnit.class))).thenReturn(false);
            when(lock.isAcquiredInThisProcess()).thenReturn(false);

            var supplier = new TrackingSupplier();
            var result = lockHelper.withLock(lock, 2, TimeUnit.SECONDS, supplier);

            var lockAcquisitionFailure = assertIsExactType(result, WithLockResult.LockAcquisitionFailure.class);

            var timeoutResult = assertIsExactType(lockAcquisitionFailure.acquisitionResult(), LockAcquisitionFailureResult.Timeout.class);
            assertThat(timeoutResult.period()).isEqualTo(Duration.ofSeconds(2));

            assertThat(supplier.wasCalled).isFalse();

            verify(lock).acquire(2, TimeUnit.SECONDS);
            verify(lock).isAcquiredInThisProcess();
            verifyNoMoreInteractions(lock);
        }

        @Test
        void shouldReleaseLock_IfSupplierThrowsAnException() throws Exception {
            when(lock.acquire(anyLong(), any(TimeUnit.class))).thenReturn(true);
            when(lock.isAcquiredInThisProcess()).thenReturn(true);

            var supplier = new ThrowingSupplier();
            var result =  lockHelper.withLock(lock, 5, TimeUnit.SECONDS, supplier);

            var lockAcquisitionFailure = assertIsExactType(result, WithLockResult.ActionFailure.class);
            assertThat(lockAcquisitionFailure.cause())
                    .isInstanceOf(UncheckedIOException.class)
                    .hasMessageContaining("I/O error")
                    .cause()
                    .isInstanceOf(IOException.class)
                    .hasMessage("I/O error");


            verify(lock).acquire(5, TimeUnit.SECONDS);
            verify(lock).isAcquiredInThisProcess();
            verify(lock).release();
            verifyNoMoreInteractions(lock);
        }
    }

    @Getter
    static class TrackingRunnable implements Runnable {

        boolean wasCalled;

        @Override
        public void run() {
            wasCalled = true;
        }
    }

    static class ThrowingRunnable implements Runnable {

        @Override
        public void run() {
            throw new UncheckedIOException(new IOException("I/O error"));
        }
    }

    @Getter
    static class TrackingSupplier implements Supplier<Long> {

        TrackingSupplier() {
            this(42L);
        }

        TrackingSupplier(Long result) {
            this.result = result;
        }

        final Long result;
        boolean wasCalled;

        @Override
        public Long get() {
            wasCalled = true;
            return result;
        }
    }

    static class ThrowingSupplier implements Supplier<Integer> {

        @Override
        public Integer get() {
            throw new UncheckedIOException(new IOException("I/O error"));
        }
    }
}
