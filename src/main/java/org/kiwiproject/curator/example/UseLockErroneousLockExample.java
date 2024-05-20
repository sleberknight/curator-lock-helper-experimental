package org.kiwiproject.curator.example;

import org.kiwiproject.curator.CuratorLockHelper2;
import org.kiwiproject.curator.UseLockResult;
import org.kiwiproject.curator.example.lock.ErroneousLock;

import java.util.concurrent.TimeUnit;

public class UseLockErroneousLockExample {

    public static void main(String[] args) {
        var helper = new CuratorLockHelper2();

        Runnable action = () -> System.out.println("Doing some work while own the lock");
        var result = helper.useLock(new ErroneousLock(), 5, TimeUnit.SECONDS, action);

        if (result instanceof UseLockResult.LockAcquisitionFailure acquisitionFailure) {
            System.out.println("** useLock - lock throws exception: Failed to obtain lock -> " + acquisitionFailure);
        } else {
            System.out.println("!! unexpected: useLock - lock throws exception. Actual result: " + result);
        }
    }
}
