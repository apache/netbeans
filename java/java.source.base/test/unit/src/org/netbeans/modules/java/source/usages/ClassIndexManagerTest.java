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
package org.netbeans.modules.java.source.usages;

import java.io.File;
import java.net.MalformedURLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.*;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public class ClassIndexManagerTest extends NbTestCase {
    
    public ClassIndexManagerTest(final String name) {
        super(name);
    }

    public void testDeadLock207855() throws Exception {
        clearWorkDir();
        final File wd = getWorkDir();
        final File cache = new File (wd,"cache");   //NOI18N
        cache.mkdir();
        IndexUtil.setCacheFolder(cache);
        final File root = new File (wd,"src");      //NOI18N
        root.mkdir();
        final Lock lock = new ReentrantLock();
        final CountDownLatch holdsLock = new CountDownLatch(1);
        final CountDownLatch inListener = new CountDownLatch(1);
        final FileChangeListener dl = new DeadLockListener(lock, inListener);
        FileUtil.addRecursiveListener(dl, cache);
        try {
            final Thread worker = new Thread() {
                @Override
                public void run() {
                    lock.lock();
                    holdsLock.countDown();
                    try {
                        inListener.await();
                        ClassIndexManager.getDefault().getUsagesQuery(Utilities.toURI(root).toURL(), true);
                    } catch (InterruptedException ie) {
                        Exceptions.printStackTrace(ie);
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        lock.unlock();
                    }
                }
            };
            worker.start();
            holdsLock.await();
            ClassIndexManager.getDefault().getUsagesQuery(Utilities.toURI(root).toURL(), true);
        } finally {
            FileUtil.removeRecursiveListener(dl, cache);
        }
    }
    
    
    private static class DeadLockListener extends FileChangeAdapter {
        
        private final Lock lck;
        private final CountDownLatch inListener;
        
        DeadLockListener(
                @NonNull final Lock lck,
                @NonNull final CountDownLatch inListener) {
            this.lck = lck;
            this.inListener = inListener;
        }

        @Override
        public void fileChanged(FileEvent fe) {
            doDeadLock();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            doDeadLock();
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            doDeadLock();
        }
        
        private void doDeadLock() {
            inListener.countDown();
            this.lck.lock();
            try {
                //NOP
            } finally {
                this.lck.unlock();
            }
        }
        
    }

}
