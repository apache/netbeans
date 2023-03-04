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
package org.netbeans.modules.masterfs.filebasedfs;

import java.io.File;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class TwoFileNamesForASingleFileTest extends NbTestCase {
    private FileObject fo;

    public TwoFileNamesForASingleFileTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        System.setSecurityManager(FileChangedManager.getInstance());
    }
    
    public void testTwoFileNames() throws Exception {
        
        File f = getWorkDir();
        fo = FileUtil.toFileObject(f);
        
        FileObject[] empty = fo.getChildren();
        assertEquals("Empty is empty", 0, empty.length);
        
        final File n = new File(f, "x.txt");
        n.createNewFile();
        FileObject fn = FileUtil.toFileObject(f);
        assertNotNull("File object found", fn);

        refresh();
        FileObject[] notEmpty = fo.getChildren();
        assertEquals("One file found", 1, notEmpty.length);

        class R implements Runnable {
            FileObject[] none;
            FileObject[] one;
            @Override
            public void run() {
                refresh();
                n.delete();
                none = fo.getChildren();
                assertTrue("Directory creation succeeds", n.mkdirs());
                refresh();
                one = fo.getChildren();
            }
        }
        R r = new R();

        Task task = RequestProcessor.getDefault().create(r);
        final Logger LOG = Logger.getLogger(FolderObj.class.getName());
        DelayingHandler delayer = new DelayingHandler(task);
        delayer.setLevel(Level.ALL);
        LOG.setLevel(Level.ALL);
        LOG.addHandler(delayer);
        task.schedule(0);
        FileObject[] block = fo.getChildren();
        assertTrue("Delayer was active", delayer.delayed);
        
        assertEquals("None is empty", 0, r.none.length);
        assertEquals("One is not empty", 1, r.one.length);
        assertEquals("One children in block as well", 1, block.length);
        assertTrue("Very likely it is a folder", block[0].isFolder());
    }
    
    final void refresh() {
        fo.refresh();
    }
    
    private class DelayingHandler extends Handler {
        volatile boolean delayed;
        final Task waitFor;
        final Thread threadToDelay;
                
        public DelayingHandler(Task waitFor) {
            this.waitFor = waitFor;
            this.threadToDelay = Thread.currentThread();
        }
        
        @Override
        public void publish(LogRecord record) {
            if (
                record.getMessage() == null || 
                !record.getMessage().contains("computeChildren, filenames")
            ) {
                return;
            }
            
            if (Thread.currentThread() == threadToDelay) {
                if (!delayed) {
                    waitFor.waitFinished();
                    delayed = true;
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
        
    }
}
