/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package threaddemo.locking;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implementation of a regular lock (read/write).
 * @author Jesse Glick
 */
final class ReadWriteLockWrapper implements DuplexLock {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    /** workaround needed in Tiger; see {@link #canRead} */
    private final ThreadLocal<Integer> reading = new ThreadLocal<Integer>() {
        protected Integer initialValue() {
            return 0;
        }
    };

    public ReadWriteLockWrapper() {}

    public void enterRead() {
        lock.readLock().lock();
        reading.set(reading.get() + 1);
    }

    public void exitRead() {
        lock.readLock().unlock();
        assert reading.get() > 0;
        reading.set(reading.get() - 1);
    }

    public void enterWrite() {
        lock.writeLock().lock();
    }

    public void exitWrite() {
        lock.writeLock().unlock();
    }

    public void read(Runnable action) {
        enterRead();
        try {
            action.run();
        } finally {
            exitRead();
        }
    }

    public void write(Runnable action) {
        enterWrite();
        try {
            action.run();
        } finally {
            exitWrite();
        }
    }

    public <T> T read(LockAction<T> action) {
        enterRead();
        try {
            return action.run();
        } finally {
            exitRead();
        }
    }

    public <T> T write(LockAction<T> action) {
        enterWrite();
        try {
            return action.run();
        } finally {
            exitWrite();
        }
    }

    public <T, E extends Exception> T read(LockExceptionAction<T, E> action) throws E {
        enterRead();
        try {
            return action.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            @SuppressWarnings("unchecked")
            E _e = (E) e;
            throw _e;
        } finally {
            exitRead();
        }
    }

    public <T, E extends Exception> T write(LockExceptionAction<T, E> action) throws E {
        enterWrite();
        try {
            return action.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            @SuppressWarnings("unchecked")
            E _e = (E) e;
            throw _e;
        } finally {
            exitWrite();
        }
    }

    public void readLater(final Runnable action) {
        Worker.start(new Runnable() {
            public void run() {
                read(action);
            }
        });
    }

    public void writeLater(final Runnable action) {
        Worker.start(new Runnable() {
            public void run() {
                write(action);
            }
        });
    }

    public boolean canRead() {
        // XXX in JDK 6 can just use: return lock.getReadHoldCount() > 0;
        return reading.get() > 0;
    }

    public boolean canWrite() {
        return lock.isWriteLockedByCurrentThread();
    }
    
}
