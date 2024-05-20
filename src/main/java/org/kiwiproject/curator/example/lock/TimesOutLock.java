package org.kiwiproject.curator.example.lock;

import org.apache.curator.framework.recipes.locks.InterProcessLock;

import java.util.concurrent.TimeUnit;

public class TimesOutLock implements InterProcessLock {

    @Override
    public void acquire() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean acquire(long time, TimeUnit unit) throws Exception {
       return false;
    }

    @Override
    public void release() throws Exception {
        // no-op
    }

    @Override
    public boolean isAcquiredInThisProcess() {
        return true;
    }
}
