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
package org.netbeans.modules.project.libraries;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
final class FileLockManager {

    //@GuardedBy("FileLockManager.class")
    private static volatile FileLockManager INSTANCE;

    //@GuardedBy("locks");
    private final Map<FileObject,ReadWriteLock> locks = Collections.synchronizedMap(new WeakHashMap<FileObject, ReadWriteLock>());

    private FileLockManager() {}

    <T> T readAction(
            @NonNull final FileObject file,
            @NonNull final Callable<T> action) throws Exception {
        Parameters.notNull("file", file);   //NOI18N
        Parameters.notNull("action", action);   //NOI18N
        final ReadWriteLock lck = getLock(file);
        lck.readLock().lock();
        try {
            return action.call();
        } finally {
            lck.readLock().unlock();
        }
    }

    <T> T writeAction(
            @NonNull final FileObject file,
            @NonNull final Callable<T> action) throws Exception {
        Parameters.notNull("file", file);       //NOI18N
        Parameters.notNull("action", action);   //NOI18N
        final ReadWriteLock lck = getLock(file);
        lck.writeLock().lock();
        try {
            return action.call();
        } finally {
            lck.writeLock().unlock();
        }
    }

    @NonNull
    private ReadWriteLock getLock(@NonNull final FileObject file) {
        synchronized (locks) {
            ReadWriteLock lck = locks.get(file);
            if (lck == null) {
                lck = new ReentrantReadWriteLock();
                locks.put(file, lck);
            }
            return lck;
        }
    }

    @NonNull
    static FileLockManager getDefault() {
        FileLockManager res = INSTANCE;
        if (res == null) {
            synchronized (FileLockManager.class) {
                res = INSTANCE;
                if (res == null) {
                    res = INSTANCE = new FileLockManager();
                }
            }
        }
        return res;
    }

}
