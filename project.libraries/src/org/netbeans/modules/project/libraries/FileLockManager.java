/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
