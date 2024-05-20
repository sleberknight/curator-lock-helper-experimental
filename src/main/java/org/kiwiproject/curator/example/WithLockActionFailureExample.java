package org.kiwiproject.curator.example;

import org.kiwiproject.curator.CuratorLockHelper2;
import org.kiwiproject.curator.WithLockResult;
import org.kiwiproject.curator.example.lock.SuccessfulLock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class WithLockActionFailureExample {

    public static void main(String[] args) {
        var helper = new CuratorLockHelper2();
        Supplier<Long> badSupplier = () -> {
            throw new RuntimeException("I am a bad supplier");
        };

        var result = helper.withLock(new SuccessfulLock(), 5, TimeUnit.SECONDS, badSupplier);

        if (result instanceof WithLockResult.ActionFailure<Long> actionFailure) {
            System.out.println("** withLock - supplier throws exception: Action failed with cause -> " + actionFailure.cause());
        } else {
            System.out.println("!! unexpected: withLock - supplier throws exception. Actual result: " + result);
        }
    }
}
