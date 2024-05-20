package org.kiwiproject.curator.example;

import org.kiwiproject.curator.CuratorLockHelper2;
import org.kiwiproject.curator.UseLockResult;
import org.kiwiproject.curator.example.lock.TimesOutLock;

import java.util.concurrent.TimeUnit;

public class UseLockTimeOutExample {

    public static void main(String[] args) {
        var helper = new CuratorLockHelper2();

        Runnable action = () -> System.out.println("Doing some work while own the lock");
        var result = helper.useLock(new TimesOutLock(), 5, TimeUnit.SECONDS, action);

        if (result instanceof UseLockResult.LockAcquisitionFailure acquisitionFailure) {
            System.out.println("** useLock - timeout: Failed to obtain lock -> " + acquisitionFailure);
        } else {
            System.out.println("!! unexpected: useLock - timeout. Actual result: " + result);
        }
    }
}
