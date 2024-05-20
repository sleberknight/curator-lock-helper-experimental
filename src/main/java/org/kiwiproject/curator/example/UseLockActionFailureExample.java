package org.kiwiproject.curator.example;

import org.kiwiproject.curator.CuratorLockHelper2;
import org.kiwiproject.curator.UseLockResult;
import org.kiwiproject.curator.example.lock.SuccessfulLock;

import java.util.concurrent.TimeUnit;

public class UseLockActionFailureExample {

    public static void main(String[] args) {
        var helper = new CuratorLockHelper2();

        Runnable badAction = () -> {
            throw new RuntimeException("I am a bad action");
        };
        var result = helper.useLock(new SuccessfulLock(), 5, TimeUnit.SECONDS, badAction);

        if (result instanceof UseLockResult.ActionFailure actionFailure) {
            System.out.println("** useLock - action throws exception: Action failed -> " + actionFailure.cause());
        } else {
            System.out.println("!! unexpected: useLock - action throws exception. Actual result: " + result);
        }
    }
}
