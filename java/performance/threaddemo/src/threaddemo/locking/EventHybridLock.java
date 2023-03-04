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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// XXX give preference to waiting writers: first to enter after last reader leaves; no new readers
// XXX handle interactions with other locks
// XXX forbid R -> W in AWT if have explicitly entered R

/**
 * Special "event hybrid" lock.
 * If in AWT, automatically canRead, and read access is a no-op.
 * Write access from AWT is OK.
 * Write access otherwise calls invokeAndWait.
 * @author Jesse Glick
 */
final class EventHybridLock implements RWLock {
    
    public static final RWLock DEFAULT = new EventHybridLock();
    
    private EventHybridLock() {}
    
    public <T> T read(LockAction<T> action) {
        if (EventLock.isDispatchThread()) {
            // Fine, go ahead.
            if (semaphore == -1) {
                semaphore = -2;
                try {
                    return action.run();
                } finally {
                    semaphore = -1;
                }
            } else {
                boolean oldReadingInAwt = readingInAwt;
                readingInAwt = true;
                try {
                    return action.run();
                } finally {
                    readingInAwt = oldReadingInAwt;
                }
            }
        } else {
            // Need to make sure no writers are running.
            Thread reader;
            synchronized (this) {
                while (semaphore < 0) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new IllegalStateException(e.toString());
                    }
                }
                semaphore++;
                Thread curr = Thread.currentThread();
                if (readers.add(curr)) {
                    reader = curr;
                } else {
                    reader = null;
                }
            }
            try {
                return action.run();
            } finally {
                synchronized (this) {
                    semaphore--;
                    if (reader != null) {
                        readers.remove(reader);
                    }
                    notifyAll();
                }
            }
        }
    }
    
    private static final class Holder<T, E extends Exception> {
        public final T object;
        public final E exception;
        public Holder(T object) {
            this.object = object;
            this.exception = null;
        }
        public Holder(E exception) {
            this.object = null;
            this.exception = exception;
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <T, E extends Exception> LockAction<Holder<T,E>> convertExceptionAction(final LockExceptionAction<T,E> action) {
        return new LockAction<Holder<T,E>>() {
            public Holder<T,E> run() {
                try {
                    return new Holder<T,E>(action.run());
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    return new Holder<T,E>((E) e);
                }
            }
        };
    }
    
    private static <T, E extends Exception> T finishExceptionAction(Holder<T,E> result) throws E {
        if (result.exception != null) {
            throw result.exception;
        } else {
            return result.object;
        }
    }
    
    public <T, E extends Exception> T read(LockExceptionAction<T,E> action) throws E {
        return finishExceptionAction(read(convertExceptionAction(action)));
    }
    
    public void read(final Runnable action) {
        read(convertRunnable(action));
    }
    
    public <T> T write(final LockAction<T> action) {
        if (!EventLock.isDispatchThread()) {
            // Try again in AWT.
            if (canRead()) {
                throw new IllegalStateException("Cannot go R -> W"); // NOI18N
            }
            try {
                final List<T> o = new ArrayList<T>(1);
                final Error[] err = new Error[1];
                EventLock.invokeAndWaitLowPriority(this, new Runnable() {
                    public void run() {
                        try {
                            o.add(write(action));
                        } catch (Error e) {
                            err[0] = e;
                        }
                    }
                });
                if (err[0] != null) {
                    throw err[0];
                }
                return o.get(0);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e.toString());
            } catch (InvocationTargetException e) {
                Throwable x = e.getTargetException();
                if (x instanceof RuntimeException) {
                    throw (RuntimeException)x;
                } else if (x instanceof Error) {
                    throw (Error)x;
                } else {
                    throw new IllegalStateException(x.toString());
                }
            }
        }
        // We are in AWT.
        int oldSemaphore;
        synchronized (this) {
            while (semaphore > 0) {
                // Wait for readers to finish first.
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e.toString());
                }
            }
            oldSemaphore = semaphore;
            if (semaphore == 0) {
                if (readingInAwt) {
                    throw new IllegalStateException("Cannot go R -> W"); // NOI18N
                } else {
                    semaphore = -1;
                }
            } else if (semaphore == -1) {
                // OK.
            } else if (semaphore == -2) {
                throw new IllegalStateException("Cannot go R -> W"); // NOI18N
            }
        }
        try {
            return action.run();
        } finally {
            if (oldSemaphore == 0) {
                // Exiting outermost write; permit readers to enter.
                synchronized (this) {
                    semaphore = 0;
                    notifyAll();
                }
            }
        }
    }
    
    public <T, E extends Exception> T write(LockExceptionAction<T,E> action) throws E {
        return finishExceptionAction(write(convertExceptionAction(action)));
    }
    
    public void write(final Runnable action) {
        write(convertRunnable(action));
    }
    
    private static <T> LockAction<T> convertRunnable(final Runnable action) {
        return new LockAction<T>() {
            public T run() {
                action.run();
                return null;
            }
        };
    }
    
    public void readLater(final Runnable action) {
        EventLock.invokeLaterLowPriority(this, new Runnable() {
            public void run() {
                read(action);
            }
        });
    }
    
    public void writeLater(final Runnable action) {
        EventLock.invokeLaterLowPriority(this, new Runnable() {
            public void run() {
                write(action);
            }
        });
    }
    
    public boolean canRead() {
        if (EventLock.isDispatchThread()) {
            return true;
        } else {
            return readers.contains(Thread.currentThread());
        }
    }
    
    public boolean canWrite() {
        if (EventLock.isDispatchThread()) {
            return semaphore == -1;
        } else {
            return false;
        }
    }
    
    public String toString() {
        // XXX include state info here
        return "Locks.eventHybridLock"; // NOI18N
    }
    
    /**
     * Count of active outside readers, or write lock state.
     * When positive, one or more readers outside AWT are holding the read lock.
     * When zero, the lock is uncontended (available in AWT).
     * When -1, the write lock is held.
     * When -2, the read lock is held inside the write lock.
     * In AWT, you can recursively enter either the read or write lock as much
     * as desired; semaphore tracks the current status only.
     */
    private int semaphore = 0;
    
    /**
     * If true, we are explicitly reading in AWT at the moment.
     */
    private boolean readingInAwt = false;
    
    /**
     * Set of readers which are running.
     */
    private final Set<Thread> readers = new HashSet<Thread>();
    
}
