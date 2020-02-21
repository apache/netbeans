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
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.*;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class WrongCreationEventsTestCase extends RemoteFileTestBase {

    public WrongCreationEventsTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testChangeDirectoryLink() throws Exception {
        String baseDir = null;
        try {
            final List<FileEvent> eventList = Collections.synchronizedList(new ArrayList<FileEvent>());

            FileChangeListener listener = new FileChangeListener() {
                @Override
                public void fileChanged(FileEvent fe) {
                    eventList.add(fe);
                }

                @Override
                public void fileAttributeChanged(FileAttributeEvent fe) {
                    eventList.add(fe);
                }

                @Override
                public void fileDataCreated(FileEvent fe) {
                    eventList.add(fe);
                }

                @Override
                public void fileFolderCreated(FileEvent fe) {
                    eventList.add(fe);
                }

                @Override
                public void fileRenamed(FileRenameEvent fe) {
                    eventList.add(fe);
                }

                @Override
                public void fileDeleted(FileEvent fe) {
                    eventList.add(fe);
                }
            };

            RemoteFileSystemManager.getInstance().resetFileSystem(execEnv, false);
            baseDir = mkTemp(execEnv, true);
            String creationScript =
                    "cd " + baseDir + "; " +
                    "echo 123 > file_1; " +
                    "echo abc > file_2";

            ProcessUtils.ExitStatus res1 = ProcessUtils.execute(execEnv, "sh", "-c", creationScript);
            assertEquals("Error executing script \"" + creationScript + "\": " + res1.getErrorString(), 0, res1.exitCode);

            FileSystemProvider.addRecursiveListener(listener, fs, "/");
            FileObject baseDirFO = getFileObject(baseDir);
            assertTrue("There should be no events (1)", eventList.isEmpty());
            baseDirFO.getChildren();
            assertTrue("There should be no events (2)", eventList.isEmpty());
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(WrongCreationEventsTestCase.class);
    }
}
