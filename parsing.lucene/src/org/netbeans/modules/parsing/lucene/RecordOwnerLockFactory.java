/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
