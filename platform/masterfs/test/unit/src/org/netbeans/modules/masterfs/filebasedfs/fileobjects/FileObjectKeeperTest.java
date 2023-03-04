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
package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author jhavlin
 */
public class FileObjectKeeperTest extends NbTestCase {

    /**
     * FileObject for this test's working directory.
     */
    private FileObject workDirFO;

    public FileObjectKeeperTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        workDirFO = FileUtil.toFileObject(getWorkDir());
    }

    /**
     * Test for bug 235928 - Deadlock scanning after change name of project with
     * folder.
     *
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testBug235928() throws IOException, InterruptedException {

        final Semaphore s = new Semaphore(0);

        /**
         * Listener that enables running of thread t2 while the events are
         * processed in the main thread.
         */
        final FileChangeListener fcl = new FileChangeAdapter() {
            @Override
            public void fileFolderCreated(FileEvent fe) {
                s.release(); // Thread t2 can run.
                try {
                    s.acquire(2); // Wait until thread t2 is finished.
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                FileChangeListener l = new FileChangeAdapter();
                try {
                    s.acquire();
                    // Try to add a listener, to ensure that the keeper object
                    // is not locked.
                    workDirFO.addRecursiveListener(l);
                    s.release(2);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    workDirFO.removeRecursiveListener(l);
                }
            }
        });

        workDirFO.addRecursiveListener(fcl);
        try {
            t2.start();
            // Create folder and process events.
            workDirFO.createFolder("test");
            t2.join();
        } finally {
            workDirFO.removeRecursiveListener(fcl);
        }
    }
}
