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
package org.netbeans.modules.parsing.lucene;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.lucene.store.Directory;
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

    private static final Set<DirectoryLockPair> lockHolder = ConcurrentHashMap.newKeySet();

    RecordOwnerLockFactory() throws IOException {
        super();
    }

    @Override
    public Lock obtainLock(Directory directory, String lock) throws IOException {
        DirectoryLockPair dlp = new DirectoryLockPair(directory, lock);
        if(! lockHolder.add(dlp)) {
            throw new LockObtainFailedException("Pair already locked: " + dlp);
        }
        return new RecordOwnerLock(dlp);
    }

    boolean hasLocks() {
        return ! lockHolder.isEmpty();
    }

    Set<DirectoryLockPair> forceClearLocks() {
        Set<DirectoryLockPair> oldLocked;
        synchronized (lockHolder) {
            oldLocked = new HashSet<>(lockHolder);
            lockHolder.clear();
        }
        return oldLocked;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append('['); //NOI18N
        sb.append(lockHolder.toString());
        sb.append("]\n"); //NOI18N
        return sb.toString();
    }


    private final class RecordOwnerLock extends Lock {

        private final Thread owner;
        private final Exception caller;
        private final DirectoryLockPair lockedPair;

        private RecordOwnerLock(DirectoryLockPair lockedPair) {
            this.lockedPair = lockedPair;
            this.owner = Thread.currentThread();
            this.caller = new Exception();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(this.getClass().getSimpleName());
            sb.append("owned by:[");    //NOI18N
            sb.append(owner);
            sb.append('(').append(owner == null ? -1 : owner.getId()).append(')');  //NOI18N
            sb.append("created from:\n");
            stackTrace(caller == null ? new StackTraceElement[0] : caller.getStackTrace(), sb);
            return sb.toString();
        }

        @Override
        public void close() throws IOException {
            lockHolder.remove(this.lockedPair);
        }

        @Override
        public void ensureValid() throws IOException {
            if (!lockHolder.contains(lockedPair)) {
                final StringBuilder sb = new StringBuilder();
                sb.append(this.lockedPair);
                sb.append("owned by:[");    //NOI18N
                sb.append(owner);
                sb.append('(').append(owner == null ? -1 : owner.getId()).append(')');  //NOI18N
                sb.append("created from:\n");
                stackTrace(caller == null ? new StackTraceElement[0] : caller.getStackTrace(), sb);
                sb.append(" not valid anymore");
                throw new IOException(sb.toString());
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

    record DirectoryLockPair(Directory directory, String lock) {

        private static final Set<Thread> RECURSION_PROTECTION = ConcurrentHashMap.newKeySet();

        @Override
        public String toString() {
            // The directory also outputs the lock in toString(), we need to
            // protect unlimited recursion
            if(RECURSION_PROTECTION.contains(Thread.currentThread())) {
                return "<<RECURSION>>";
            }
            try {
                RECURSION_PROTECTION.add(Thread.currentThread());
                return "DirectoryLockPair{" + directory + ", " + lock + "}";
            } finally {
                RECURSION_PROTECTION.remove(Thread.currentThread());
            }
        }

    }
}
