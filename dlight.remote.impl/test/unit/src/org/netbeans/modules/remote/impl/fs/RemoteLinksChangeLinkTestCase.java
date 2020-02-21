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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.*;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class RemoteLinksChangeLinkTestCase extends RemoteFileTestBase {

    public RemoteLinksChangeLinkTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        RemoteFileObjectFactory.testSetReportUnexpected(false);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        RemoteFileObjectFactory.testSetReportUnexpected(true);
    }

    @ForAllEnvironments
    public void testChangeDirectoryLink() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);

            String fileName = "file";
            String realDir1 = baseDir + "/real_dir_1";
            String realDir2 = baseDir + "/real_dir_2";
            String linkDirName = "link_dir";
            String linkDir = baseDir + '/' + linkDirName;
            String realFile1 = realDir1 + "/" + fileName;
            String realFile2 = realDir2 + "/" + fileName;
            String linkFile1 = linkDir + "/" + fileName;

            String creationScript =
                    "cd " + baseDir + "; " +
                    "mkdir -p " + realDir1 + "; " +
                    "mkdir -p " + realDir2 + "; " +
                    "ln -s " + realDir1 + ' ' + linkDirName + "; " +
                    "echo 123 > " + realFile1 + "; " +
                    "echo abc > " + realFile2;

            ProcessUtils.ExitStatus res1 = ProcessUtils.execute(execEnv, "sh", "-c", creationScript);
            assertEquals("Error executing script \"" + creationScript + "\": " + res1.getErrorString(), 0, res1.exitCode);

            FileObject realFO, linkFO, linkDirFO;
            RemoteFileObject oldChildFO;
            
            realFO = getFileObject(realFile1);
            linkFO = getFileObject(linkFile1);
            linkDirFO = getFileObject(linkDir);
            FileObject tmpFo = linkDirFO.getFileObject(fileName);
            assertNotNull(tmpFo);
            assertTrue(tmpFo instanceof RemoteFileObject);
            oldChildFO = (RemoteFileObject) tmpFo;
            RemoteFileObjectBase oldChildFO_implementor = oldChildFO.getImplementor();

            String changeScript =
                    "cd " + baseDir + "; " +
                    "rm " + linkDirName + "; " +
                    "ln -s " + realDir2 + ' ' + linkDirName + "; " +
                    "";

            ProcessUtils.ExitStatus res2 = ProcessUtils.execute(execEnv, "sh", "-c", changeScript);
            assertEquals("Error executing script \"" + creationScript + "\": " + res1.getErrorString(), 0, res1.exitCode);

            RemoteFileObject baseDirFO = getFileObject(baseDir);
            baseDirFO.nonRecursiveRefresh();

            boolean childValid = oldChildFO.isValid();
            boolean newImplementorValid = oldChildFO.getImplementor().isValid();
            boolean oldImplementorValid = oldChildFO_implementor.isValid();
            FileObject oldParent = oldChildFO.getParent();
            boolean parentValid = oldParent.isValid();
            //FileObject[] children = oldParent.getChildren();
            assertFalse("Old implementor should be be invalid", oldImplementorValid);
            assertTrue("New implementor should be valid", newImplementorValid);
            assertTrue("Child should be valid", childValid);
            assertTrue("Parent should be valid", parentValid);
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    @ForAllEnvironments
    public void testChangedLinkListeners() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);

            String fileName = "file";
            String realDir1 = baseDir + "/real_dir_1";
            String realDir2 = baseDir + "/real_dir_2";
            String linkDirName = "link_dir";
            String linkDir = baseDir + '/' + linkDirName;
            String realFile1 = realDir1 + "/" + fileName;
            String realFile2 = realDir2 + "/" + fileName;
            String linkFile1 = linkDir + "/" + fileName;

            String creationScript =
                    "cd " + baseDir + "; " +
                    "mkdir -p " + realDir1 + "; " +
                    "mkdir -p " + realDir2 + "; " +
                    "ln -s " + realDir1 + ' ' + linkDirName + "; " +
                    "echo 123 > " + realFile1;

            ProcessUtils.ExitStatus res1 = ProcessUtils.execute(execEnv, "sh", "-c", creationScript);
            assertEquals("Error executing script \"" + creationScript + "\": " + res1.getErrorString(), 0, res1.exitCode);

            RemoteFileObject baseDirFO = getFileObject(baseDir);
            RemoteFileObject realDirFO1 = getFileObject(realDir1);
            RemoteFileObject realDirFO2 = getFileObject(realDir2);
            RemoteFileObject linkDirFO = getFileObject(linkDir);
            RemoteFileObject realFileFO1 = getFileObject(realFile1);
            RemoteFileObject realFileFO2 = getFileObject(realFile1);

            final List<FileEvent> eventList = Collections.synchronizedList(new ArrayList<FileEvent>());

            FileChangeListener listener = new FileChangeListener() {
                private void register(FileEvent fe) {
                    eventList.add(fe);
                }
                @Override
                public void fileChanged(FileEvent fe) {
                    register(fe);
                }

                @Override
                public void fileAttributeChanged(FileAttributeEvent fe) {
                    register(fe);
                }

                @Override
                public void fileDataCreated(FileEvent fe) {
                    register(fe);
                }

                @Override
                public void fileFolderCreated(FileEvent fe) {
                    register(fe);
                }

                @Override
                public void fileRenamed(FileRenameEvent fe) {
                    register(fe);
                }

                @Override
                public void fileDeleted(FileEvent fe) {
                    register(fe);
                }
            };
            linkDirFO.addFileChangeListener(listener);
            FileUtil.createData(realDirFO1, "file_2");
            assertFalse("No events came after programmatic file creatin in dir 1", eventList.isEmpty());

            String changeScript =
                    "cd " + baseDir + "; " +
                    "rm " + linkDirName + "; " +
                    "ln -s " + realDir2 + ' ' + linkDirName + "; " +
                    "";

            ProcessUtils.ExitStatus res2 = ProcessUtils.execute(execEnv, "sh", "-c", changeScript);
            assertEquals("Error executing script \"" + creationScript + "\": " + res1.getErrorString(), 0, res1.exitCode);

            baseDirFO.refresh();
            eventList.clear();
            FileUtil.createData(realDirFO1, "file_3");
            assertTrue("Event list should be empty", eventList.isEmpty());
            FileUtil.createData(realDirFO2, "file_4");
            assertFalse("No events came after programmatic file creatin in dir 1", eventList.isEmpty());


        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(RemoteLinksChangeLinkTestCase.class);
    }
}
