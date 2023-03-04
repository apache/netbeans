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

package org.netbeans.modules.settings;

import java.io.IOException;

import org.netbeans.junit.NbTestCase;


import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;

/** JUnit tests
 *
 * @author  Jan Pokorsky
 */
public final class ScheduledRequestTest extends NbTestCase {
    FileSystem fs;

    public ScheduledRequestTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        
        LocalFileSystem lfs = new LocalFileSystem();
        clearWorkDir();
        lfs.setRootDirectory(this.getWorkDir());
        fs = lfs;
    }
    
    public void testSchedule() throws Exception {
        FSA toRun = new FSA();
        FileObject fo = fs.getRoot();
        ScheduledRequest sr = new ScheduledRequest(fo, toRun);
        Object obj1 = new Object();
        sr.schedule(obj1);
        assertNotNull("none file lock", sr.getFileLock());
        for (int i = 0; i < 2 && !toRun.finished; i++) {
            Thread.sleep(2500);    
        }
        assertTrue("scheduled request was not performed yet", toRun.finished);        
        assertNull("file is still locked", sr.getFileLock());
    }

    @RandomlyFails // NB-Core-Build #2564
    public void testCancel() throws Exception {
        FSA toRun = new FSA();
        FileObject fo = fs.getRoot();
        ScheduledRequest sr = new ScheduledRequest(fo, toRun);
        Object obj1 = new Object();
        sr.schedule(obj1);
        assertNotNull("none file lock", sr.getFileLock());
        sr.cancel();
        assertNull("file lock", sr.getFileLock());
        Thread.sleep(2500);
        assertTrue("scheduled request was performed", !toRun.finished);
        
        Object obj2 = new Object();
        sr.schedule(obj2);
        assertNotNull("none file lock", sr.getFileLock());
        Thread.sleep(2500);
        assertNull("file lock", sr.getFileLock());
        assertTrue("scheduled request was not performed yet", toRun.finished);
    }
    
    public void testForceToFinish() throws Exception {
        FSA toRun = new FSA();
        FileObject fo = fs.getRoot();
        ScheduledRequest sr = new ScheduledRequest(fo, toRun);
        Object obj1 = new Object();
        sr.schedule(obj1);
        assertNotNull("none file lock", sr.getFileLock());
        sr.forceToFinish();
        assertTrue("scheduled request was not performed yet", toRun.finished);
        assertNull("file lock", sr.getFileLock());
    }
    
    public void testRunAndWait() throws Exception {
        FSA toRun = new FSA();
        FileObject fo = fs.getRoot();
        ScheduledRequest sr = new ScheduledRequest(fo, toRun);
        sr.runAndWait();
        assertTrue("scheduled request was not performed yet", toRun.finished);
        assertNull("file lock", sr.getFileLock());
    }
    
    private static class FSA implements org.openide.filesystems.FileSystem.AtomicAction {
        boolean finished = false;
        public void run() throws IOException {
            finished = true;
        }
        
    }
}
