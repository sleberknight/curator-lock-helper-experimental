package org.kiwiproject.curator.example;

import org.kiwiproject.curator.CuratorLockHelper2;
import org.kiwiproject.curator.WithLockResult;
import org.kiwiproject.curator.example.lock.ErroneousLock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class WithLockErroneousLockExample {

    public static void main(String[] args) {
        var helper = new CuratorLockHelper2();

        Supplier<Long> supplier = () -> 42L;
        var result = helper.withLock(new ErroneousLock(), 5, TimeUnit.SECONDS, supplier);

        if (result instanceof WithLockResult.LockAcquisitionFailure<Long> acquisitionFailure) {
            System.out.println("** withLock - lock throws exception: Failed to obtain lock -> " + acquisitionFailure);
        } else {
            System.out.println("!! unexpected: withLock - lock throws exception. Actual result: " + result);
        }
    }
}
