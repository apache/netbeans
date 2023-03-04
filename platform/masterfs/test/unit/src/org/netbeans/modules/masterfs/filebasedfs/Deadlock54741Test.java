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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 * @author pzajac
 */
public class Deadlock54741Test extends NbTestCase {
    private static final Logger LOG = Logger.getLogger(Deadlock54741Test.class.getName());

    private static class DelFileChangeListener implements FileChangeListener {
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        public void fileChanged(FileEvent fe) {
        }

        public void fileDataCreated(FileEvent fe) {
        }

        public void fileDeleted(FileEvent fe) {
            try {
                synchronized (this) {
                    wait(); 
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void fileFolderCreated(FileEvent fe) {
        }

        public void fileRenamed(FileRenameEvent fe) {
        }
        
    }
    
    private static class DeleteRunnable implements Runnable {
        FileObject fo;
        public DeleteRunnable(FileObject fo) {
            this.fo = fo;
        }
        public void run() {
            LOG.fine("start delete");
            try {
               fo.getFileSystem().addFileChangeListener(new DelFileChangeListener());
               FileSystem fs = fo.getFileSystem(); 
               FileUtil.toFile(fo).delete();
               fs.refresh(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            LOG.fine("end delete");
        } 
    }
    
    
    public Deadlock54741Test(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    public void testDeadLock () throws Exception {
        clearWorkDir();
        File f = File.createTempFile("fff", "fsdfsd", getWorkDir());
        FileObject tmpFo = FileUtil.toFileObject(f.getParentFile()); 
        assertNotNull(tmpFo);
      
        FileObject fo = null;
        fo = tmpFo.createData("ssss");   
        Runnable deleteRunnable = new DeleteRunnable(fo); 
        Thread thread = new Thread(deleteRunnable);
        thread.start();
            
        try {
            Thread.sleep(2000);
            boolean isDeadlock [] = new boolean[1]; 
             makeDeadlock(tmpFo,f, isDeadlock);   
            Thread.sleep(2000);
            boolean isD = isDeadlock[0];
            // finish -> unlock thread
            synchronized (deleteRunnable) {
                deleteRunnable.notify();
            }
            assertFalse("deadlock!!!",isD);    
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        
    }
    private Thread makeDeadlock(final FileObject fo, final File f,final boolean isDeadLock[]) {  
        isDeadLock[0] = true;
        Thread t = new Thread () {
            @Override
            public void run() {
                fo.getFileObject(f.getName());
                isDeadLock[0] = false;
            }
        };
        t.start(); 
        return t;  
    }  
        
}
