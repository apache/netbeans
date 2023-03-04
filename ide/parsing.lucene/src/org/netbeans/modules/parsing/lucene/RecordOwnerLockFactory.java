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
package org.netbeans.modules.parsing.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.LockObtainFailedException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
//@ThreadSafe
class RecordOwnerLockFactory extends LockFactory {

    private final Map<String,/*@GuardedBy("locks")*/RecordOwnerLock> locks =
        new HashMap<>();

    RecordOwnerLockFactory() throws IOException {
        super();
    }

    @Override
    public Lock makeLock(String lockName) {
        synchronized (locks) {
            RecordOwnerLock res = locks.get(lockName);
            if (res == null) {
                res = new RecordOwnerLock();
                locks.put(lockName, res);
            }
            return res;
        }
    }

    @Override
    public void clearLock(String lockName) throws IOException {
        synchronized (locks) {
            final RecordOwnerLock lock = locks.remove(lockName);
            if (lock != null) {
                lock.release();
            }
        }
    }

    boolean hasLocks() {
        synchronized (locks) {
            boolean res = false;
            for (RecordOwnerLock lock : locks.values()) {
                res|=lock.isLocked();
            }
            return res;
        }
    }

    Collection<? extends Lock> forceClearLocks() {
        synchronized (locks) {
            final Queue<RecordOwnerLock> locked = new ArrayDeque<>();
            for (Iterator<RecordOwnerLock> it = locks.values().iterator();
                it.hasNext();) {
                RecordOwnerLock lock = it.next();
                if (lock.isLocked()) {
                    it.remove();
                    locked.offer(lock);
                }
            }
            return locked;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append('['); //NOI18N
        synchronized (locks) {
            boolean first = true;
            for (Map.Entry<String,RecordOwnerLock> e : locks.entrySet()) {
                if (!first) {
                    sb.append('\n');    //NOI18N
                } else {
                    first = false;
                }
                sb.append("name: ").append(e.getKey()).append("->").append(e.getValue());   //NOI18N
            }
        }
        sb.append("]\n"); //NOI18N
        return sb.toString();
    }


    private final class RecordOwnerLock extends Lock {

        //@GuardedBy("locks")
        private Thread owner;
        //@GuardedBy("locks")
        private Exception caller;

        private RecordOwnerLock() {
        }

        @Override
        public boolean obtain() {
            synchronized (RecordOwnerLockFactory.this.locks) {
                if (this.owner == null) {
                    this.owner = Thread.currentThread();
                    this.caller = new Exception();
                    return true;
                } else {
                    return false;
                }
            }
        }

        @Override
        public boolean obtain(long lockWaitTimeout) throws LockObtainFailedException, IOException {
            try {
                return super.obtain(lockWaitTimeout);
            } catch (LockObtainFailedException e) {
                throw annotateException(
                    e,
                    (File) null,
                    Thread.getAllStackTraces(),
                    RecordOwnerLockFactory.this);
            }
        }

        @Override
        public void release() {
            synchronized (RecordOwnerLockFactory.this.locks) {
                this.owner = null;
                this.caller = null;
            }
        }

        @Override
        public boolean isLocked() {
            synchronized (RecordOwnerLockFactory.this.locks) {
                return this.owner != null;
            }
        }

        @Override
        public String toString() {
            synchronized (RecordOwnerLockFactory.this.locks) {
                final StringBuilder sb = new StringBuilder();
                sb.append(this.getClass().getSimpleName());
                sb.append("owned by:[");    //NOI18N
                sb.append(owner);
                sb.append('(').append(owner == null ? -1 : owner.getId()).append(')');  //NOI18N
                sb.append("created from:\n");
                stackTrace(caller == null ? new StackTraceElement[0] : caller.getStackTrace(), sb);
                return sb.toString();
            }
        }
    }

    static <T extends Exception> T annotateException(
        @NonNull final T e,
        @NullAllowed File indexDir,
        @NullAllowed Map<Thread,StackTraceElement[]> threads) {
        return annotateException(e, indexDir, threads, null);
    }

    private static <T extends Exception> T annotateException(
        @NonNull final T e,
        @NullAllowed File indexDir,
        @NullAllowed Map<Thread,StackTraceElement[]> threads,
        @NullAllowed LockFactory lockFactory) {
        final StringBuilder message = new StringBuilder();
        if (indexDir != null) {
            final File[] children = indexDir.listFiles();
            if (children == null) {
                message.append("Non existing index folder");    //NOI18N
            } else {
                for (File c : children) {
                    message.append(c.getName()).append(" f: ").append(c.isFile()).
                    append(" r: ").append(c.canRead()).
                    append(" w: ").append(c.canWrite()).append("\n");  //NOI18N
                }
            }
        }
        if (threads != null) {
            final Thread ct = Thread.currentThread();
            message.append("current thread: ").append(ct).append('(').append(ct.getId()).append(')');    //NOI18N
            message.append("threads: \n");     //NOI18N   //NOI18N
            stackTraces(threads, message);
        }
        if (lockFactory != null) {
            message.append("lockFactory: ").append(lockFactory);    //NOI18N
        }
        return Exceptions.attachMessage(e, message.toString());
    }

    private static void stackTraces(
        @NonNull final Map<Thread,StackTraceElement[]> traces,
        @NonNull final StringBuilder sb) {
        for (Map.Entry<Thread,StackTraceElement[]> entry : traces.entrySet()) {
            final Thread t = entry.getKey();
            sb.append(t).append('(').append(t.getId()).append(")\n"); //NOI18N
            stackTrace(entry.getValue(), sb);
        }
    }

    private static void stackTrace(
        @NonNull final StackTraceElement[] stack,
        @NonNull final StringBuilder sb) {
        for (StackTraceElement se : stack) {
            sb.append('\t').append(se).append('\n');    //NOI18N
        }
    }
}
