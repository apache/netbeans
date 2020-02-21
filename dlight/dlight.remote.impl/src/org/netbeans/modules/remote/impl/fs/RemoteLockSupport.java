/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.remote.impl.fs;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;

/**
 *
 */
public class RemoteLockSupport {

    private final Object mainLock = new Object();
    private final Map<File, WeakReference<ReadWriteLock>> cacheLocks = new HashMap<>();
    private final IdentityHashMap<RemoteFileObjectBase, RemoteFileLock> fileLocks = new IdentityHashMap<>();

    private final Object plainFileMainLock = new Object();
    private final Map<RemotePlainFile, SimpleRWLock> plainFileRWLocks = new IdentityHashMap<>();

    private static final int RW_LOCK_TIMEOUT = Integer.getInteger("remote.rwlock.timeout", 4); // NOI18N
    private static final boolean TRACE_LOCKERS;
    static {
        boolean trace = RemoteLogger.isDebugMode();
        String setting = System.getProperty("remote.rwlock.trace.lockers"); //NNOI18N
        if (setting != null) {
            trace = Boolean.parseBoolean(setting);
        }
        TRACE_LOCKERS = trace;
    }

    private AtomicInteger readLocksCount = new AtomicInteger();
    private AtomicInteger writeLocksCount = new AtomicInteger();

    /** 
     * Get read-write lock related to the given file object cache
     */
    public ReadWriteLock getCacheLock(RemoteFileObjectWithCache fo) {
        final File file = fo.getCache();
        synchronized (mainLock) {
            WeakReference<ReadWriteLock> ref = cacheLocks.get(file);
            ReadWriteLock result = (ref == null) ? null : ref.get();
            if (result == null) {
                result = new ReentrantReadWriteLock();
                cacheLocks.put(file, new WeakReference<>(result));
            }
            return result;
        }
    }

    public FileLock lock(RemoteFileObjectBase fo) throws FileAlreadyLockedException {
        RemoteFileLock lock;
        synchronized (mainLock) {
            lock = fileLocks.get(fo);
            if (lock != null && lock.isValid()) {
                throw new FileAlreadyLockedException(fo.getPath());
            }
            lock = new RemoteFileLock(fo);
            fileLocks.put(fo, lock);
        }
        return lock;
    }

    public boolean isLocked(RemoteFileObjectBase fo) {
        synchronized (mainLock) {
            RemoteFileLock lock = fileLocks.get(fo);
            return lock != null && lock.isValid();
        }
    }

    public boolean checkLock(RemoteFileObjectBase fo, FileLock aLock) {
        if (aLock != null) {
            synchronized (mainLock) {
                RemoteFileLock lock = fileLocks.get(fo);
                return lock == aLock;
            }
        }
        return true;
    }

    private SimpleRWLock getPlainFileRWLock(RemotePlainFile fo, boolean create) {
        SimpleRWLock lock;
        synchronized (plainFileMainLock) {
            lock = plainFileRWLocks.get(fo);
            if (lock == null && create) {
                lock = TRACE_LOCKERS ? new SimpleRWLockWithTrace(fo) : new SimpleRWLock(fo);
                plainFileRWLocks.put(fo, lock);
            }
        }
        return lock;
    }

    public void tryReadLock(RemotePlainFile fo) throws InterruptedException, FileAlreadyLockedException {
        getPlainFileRWLock(fo, true).tryReadLock();
    }

    public void readUnlock(RemotePlainFile fo) {
        SimpleRWLock lock = getPlainFileRWLock(fo, false);
        if (lock != null) {
            lock.readUnlock();
        }
    }

    public void tryWriteLock(RemotePlainFile fo) throws InterruptedException, FileAlreadyLockedException {
        getPlainFileRWLock(fo, true).tryWriteLock();
    }

    public void writeUnlock(RemotePlainFile fo) {
        SimpleRWLock lock = getPlainFileRWLock(fo, false);
        if (lock != null) {
            lock.writeUnlock();
        }
    }

    void printStatistics(RemoteFileSystem fs) {
        System.err.println("RemoteLockSupport statistics for " + fs);
        System.err.println("Read locks count:  " + readLocksCount.get());
        System.err.println("Write locks count: " + writeLocksCount.get());
    }

    // This homemade Read-Write lock is used instead of ReentrantReadWriteLock to support unlocking from
    // the thread other when one acquired the lock. This is required by FileObjectTestHid.testBigFileAndAsString test.
    // In brief the problem is the following: testBigFileAndAsString checks that if FileObject's InputStream is not closed
    // properly it will be closed in the finalizer. But it is not possible to unlock ReentrantReadWriteLock read lock
    // from the finalizer as it is executed in separate thread: the exception will happen if you try. And this homemade lock
    // do not have this restriction.
    // Some facts about RWL implementation can be found here: http://java.dzone.com/news/java-concurrency-read-write-lo
    private class SimpleRWLock {

        private int activeReaders = 0;
        private int waiters = 0;
        private Thread writer = null;
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition readable = lock.newCondition();
        private final Condition writtable = lock.newCondition();
        private final RemotePlainFile owner;

        public SimpleRWLock(RemotePlainFile owner) {
            this.owner = owner;
        }

        private boolean writeCondition() {
            assert lock.isLocked();
            return activeReaders == 0 && writer == null;
        }

        // should support lock's downgrading
        private boolean readCondition() {
            assert lock.isLocked();
            return writer == null || writer == Thread.currentThread();
        }

        private void removeIfPossible() {
            assert lock.isLocked();
            if (activeReaders == 0 && writer == null && waiters == 0) {
                plainFileRWLocks.remove(owner);
            }
        }

        protected void onReadLock(boolean success) {
        }

        protected void onReadUnlock() {
        }

        protected void onWriteLock(boolean success) {
        }

        protected void onWriteUnlock() {
        }

        public Thread getWriter() {
            return writer;
        }

        public RemotePlainFile getOwner() {
            return owner;
        }

        public void tryReadLock() throws InterruptedException, FileAlreadyLockedException {
            lock.lock();
            try {
                waiters++;
                while (!readCondition()) {
                    if (!readable.await(RW_LOCK_TIMEOUT, TimeUnit.SECONDS)) {                        
                        onReadLock(false);
                        throw createReadLockException();
                    }
                }
                activeReaders++;
                if (writer == Thread.currentThread()) {
                    writer = null;
                }
                readLocksCount.incrementAndGet();
                onReadLock(true);
            } finally {
                waiters--;
                lock.unlock();
            }
        }

        public void readUnlock() {
            lock.lock();
            try {
                waiters++;
                if (activeReaders > 0) {
                    activeReaders--;
                    if (activeReaders == 0) {
                        writtable.signalAll();
                    }
                }
                onReadUnlock();
            } finally {
                waiters--;
                removeIfPossible();
                lock.unlock();
            }
        }

        public void tryWriteLock() throws InterruptedException, FileAlreadyLockedException {
            lock.lock();
            try {
                while (!writeCondition()) {
                    if (!writtable.await(RW_LOCK_TIMEOUT, TimeUnit.SECONDS)) {
                        onWriteLock(false);
                        throw createWriteLockException();
                    }
                }
                writer = Thread.currentThread();
                writeLocksCount.incrementAndGet();
                onWriteLock(true);
            } finally {                
                lock.unlock();
            }
        }

        public void writeUnlock() {
            lock.lock();
            try {
                if (writer != null) {
                    writer = null;
                    writtable.signal();
                    readable.signalAll();
                }
            } finally {
                removeIfPossible();
                lock.unlock();
            }
        }

        protected FileAlreadyLockedException createReadLockException() {
            return new FileAlreadyLockedException("Cannot read from locked file: " + owner); //NOI18N
        }

        protected FileAlreadyLockedException createWriteLockException() {
            return new FileAlreadyLockedException("Cannot write to locked file: " + owner); //NOI18N
        }
    }

    private static class ReaderInfo {
        public final Thread thread;
        public final StackTraceElement[] lockedStack;
        public final long timestamp;
        public ReaderInfo next;
        public ReaderInfo(ReaderInfo next) {
            this.next = next;
            this.thread = Thread.currentThread();
            this.timestamp = System.currentTimeMillis();
            StackTraceElement[] stack = thread.getStackTrace();
            this.lockedStack = new StackTraceElement[stack.length-4];
            System.arraycopy(stack, 4, lockedStack, 0, stack.length-4);
        }
    }

    private class SimpleRWLockWithTrace extends SimpleRWLock {

        // Is accessed from onReadLock, onReadUnLock, onWriteLock, onWriteUnlock -
        // all these are called under parent's lock => no need to sync
        private ReaderInfo lastReader;

        public SimpleRWLockWithTrace(RemotePlainFile owner) {
            super(owner);
        }

        private void addReader() {
            lastReader = new ReaderInfo(lastReader);
        }

        private void removeReader(ReaderInfo reader) {
            if (reader == lastReader) {
                lastReader = lastReader.next;
            } else {
                ReaderInfo prev = lastReader;
                while (prev != null) {
                    ReaderInfo next = prev.next;
                    if (next == reader) {
                        prev.next = next.next;
                    }
                    prev = next;
                }
            }
        }

        private ReaderInfo findReader() {
            ReaderInfo curr = lastReader;
            while (curr != null) {
                if (curr.thread.getId() == Thread.currentThread().getId()) {
                    return curr;
                }
                curr = curr.next;
            }
            return lastReader;
        }

        @Override
        protected void onReadLock(boolean success) {
            if (success) {
                addReader();
            }
        }

        @Override
        protected void onReadUnlock() {
            ReaderInfo reader = findReader();
            if (reader != null) {
                removeReader(reader);
            }
        }

        @Override
        protected FileAlreadyLockedException createWriteLockException() {
            FileAlreadyLockedException result = super.createWriteLockException();
            Exception ex = result;
            ReaderInfo reader = lastReader;
            while (reader != null) {
                Exception cause = new Exception("Locked at " + new Date(reader.timestamp) + " by thread " + reader.thread.getName()); //NOI18N
                cause.setStackTrace(reader.lockedStack);
                ex.initCause(cause);
                ex = cause;
                StackTraceElement[] stack = reader.thread.getStackTrace();
                if (stack.length > 0) {
                    cause = new Exception("Now the stack of the reader thead " + reader.thread.getName()); //NOI18N
                    cause.setStackTrace(stack);
                    ex.initCause(cause);
                    ex = cause;
                }
                reader = reader.next;
            }
            return result;
        }
    }

    private class RemoteFileLock extends FileLock {

        private final RemoteFileObjectBase fo;

        public RemoteFileLock(RemoteFileObjectBase fo) {
            this.fo = fo;
        }

        @Override
        public void releaseLock() {
            synchronized (mainLock) {
                super.releaseLock();
                fileLocks.remove(fo);
            }
        }
    }
}
