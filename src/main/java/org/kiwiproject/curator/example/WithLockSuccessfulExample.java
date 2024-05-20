package org.kiwiproject.curator.example;

import org.kiwiproject.curator.CuratorLockHelper2;
import org.kiwiproject.curator.WithLockResult;
import org.kiwiproject.curator.example.lock.SuccessfulLock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class WithLockSuccessfulExample {

    public static void main(String[] args) {
        var helper = new CuratorLockHelper2();

        Supplier<Long> supplier = () -> 42L;
        var result = helper.withLock(new SuccessfulLock(), 5, TimeUnit.SECONDS, supplier);

        if (result instanceof WithLockResult.Success<Long> successResult) {
            var theResult = successResult.result();
            System.out.println("** withLock - success: Used the lock! -> result: " + successResult + ", with returned value: " + theResult);
        } else {
            System.out.println("!! unexpected: withLock - success. Actual result: " + result);
        }
    }
}
