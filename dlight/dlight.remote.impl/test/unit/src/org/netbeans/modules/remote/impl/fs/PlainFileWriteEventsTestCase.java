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

package org.netbeans.modules.remote.impl.fs;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;

/**
 *
 */
public class PlainFileWriteEventsTestCase extends RemoteFileTestBase {

//    static {
//        //System.setProperty("remote.fs_server", "false");
//        //System.setProperty("remote.fs_server.verbose", "3");
//        System.setProperty("remote.fs_server.suppress.stderr", "false");
//        //System.setProperty("remote.fs_server.refresh", "60000"); // NOI18N
//    }    
        
    public PlainFileWriteEventsTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }
    
    @ForAllEnvironments
    public void testFileWriteEvents() throws Exception {
        String remoteBaseDir = null;
        FileLock lock = null;
        OutputStream os = null;
        try {
            remoteBaseDir = mkTempAndRefreshParent(true);            
            FileObject subDirFO = getFileObject(remoteBaseDir).createFolder("testFileWriteEvents");            
            FileObject fo1 = subDirFO.createData("text.txt");
            List<FileEvent> events = new ArrayList<>();
            subDirFO.getFileSystem().addFileChangeListener(new CollectingFileChangeListener(events));
            sleep(3000);
            lock = fo1.lock();
            os = fo1.getOutputStream(lock);
            fileChangedAssert("No event should be fired", fo1, events, 0);
            os.write("alkdsakldsaklafdsaklfalkfaklfalkf".getBytes());
            os.close ();
            os = null;
            fileChangedAssert("Only one event should be fired", fo1, events, 1);
            fo1.refresh(false);
            fileChangedAssert("Unexpected event", fo1, events, 1);
        } finally {
            if (lock != null) {
                lock.releaseLock();
            }
            if (os != null) {
                os.close();
            }            
            removeRemoteDirIfNotNull(remoteBaseDir);            
        }
    }

    private void fileChangedAssert(String message, FileObject fo, List<FileEvent> list, int expectedCount) {
        assertTrue(message + " for " + fo + " Fired: " +list.size () + " , but expected: " + expectedCount, expectedCount == list.size ()); 
    }
    
    public static Test suite() {
        return RemoteApiTest.createSuite(PlainFileWriteEventsTestCase.class);
    }

    private class CollectingFileChangeListener implements FileChangeListener {
        
        private final List<FileEvent> eventList;

        public CollectingFileChangeListener(List<FileEvent> eventList) {
            this.eventList = eventList;
        }
        
        @Override
        public void fileFolderCreated(FileEvent fe) {
            eventList.add(fe);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            eventList.add(fe);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            eventList.add(fe);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            eventList.add(fe);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            eventList.add(fe);
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            eventList.add(fe);
        }
    }    
}
