/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
