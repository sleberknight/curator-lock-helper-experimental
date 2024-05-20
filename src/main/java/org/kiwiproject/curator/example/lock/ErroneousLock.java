package org.kiwiproject.curator.example.lock;

import org.apache.curator.framework.recipes.locks.InterProcessLock;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("java:S112")
public class ErroneousLock implements InterProcessLock {

    @Override
    public void acquire() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean acquire(long time, TimeUnit unit) throws Exception {
        throw new Exception("Some kind of Zookeeper error");
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
