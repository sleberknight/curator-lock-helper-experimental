package org.kiwiproject.curator.example;

import org.kiwiproject.curator.CuratorLockHelper2;
import org.kiwiproject.curator.WithLockResult;
import org.kiwiproject.curator.example.lock.TimesOutLock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class WithLockTimeOutExample {

    public static void main(String[] args) {
        var helper = new CuratorLockHelper2();

        Supplier<Long> supplier = () -> 42L;
        var result = helper.withLock(new TimesOutLock(), 5, TimeUnit.SECONDS, supplier);

        if (result instanceof WithLockResult.LockAcquisitionFailure<Long> acquisitionFailure) {
            System.out.println("** withLock - timeout: Failed to obtain lock -> " + acquisitionFailure);
        } else {
            System.out.println("!! unexpected: withLock - timeout. Actual result: " + result);
        }
    }
}
