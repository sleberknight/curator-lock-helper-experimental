package org.kiwiproject.curator.example;

import org.kiwiproject.curator.CuratorLockHelper2;
import org.kiwiproject.curator.UseLockResult;
import org.kiwiproject.curator.example.lock.SuccessfulLock;

import java.util.concurrent.TimeUnit;

public class UseLockSuccessfulExample {

    public static void main(String[] args) {
        var helper = new CuratorLockHelper2();

        Runnable action = () -> System.out.println("Doing some work while own the lock");
        var result = helper.useLock(new SuccessfulLock(), 5, TimeUnit.SECONDS, action);

        if (result instanceof UseLockResult.Success successResult) {
            System.out.println("** useLock - success: Used the lock! -> result: " + successResult.toString());
        } else {
            System.out.println("!! unexpected: useLock - success. Actual result: " + result);
        }
    }
}
