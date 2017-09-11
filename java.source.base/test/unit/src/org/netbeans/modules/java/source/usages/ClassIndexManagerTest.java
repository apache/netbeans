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
