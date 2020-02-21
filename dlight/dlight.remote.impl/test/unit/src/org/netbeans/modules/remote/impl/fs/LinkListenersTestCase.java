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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class LinkListenersTestCase extends RemoteFileTestBase {

    public LinkListenersTestCase(String testName) {
        super(testName);
    }
    
    public LinkListenersTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }


    private void doTestListeners() throws Throwable {
        String baseDir = mkTempAndRefreshParent(true);
        File workDir = getWorkDir();
        File log = new File(workDir, "remote.dat");            
        PrintStream out = new PrintStream(log);
        try {                        
            final String childName = "child_file.h";
            final String subdirName = "child_dir";
            String childLinkName = childName + ".lnk";
            String subirLinkName = subdirName + ".lnk";

            FileObject baseDirFO = getFileObject(baseDir);
            ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(baseDirFO);
            
            String[] creationData = new String[] {
                "- " + childName,
                "d " + subdirName,
                "l " + childName + ' ' + childLinkName,
                "l " + subdirName + ' ' + subirLinkName
            };
            createDirStructure(env, baseDir, creationData);
            
            
            baseDirFO.refresh();

            FileObject childLinkFO = baseDirFO.getFileObject(childLinkName);
            assertNotNull(childLinkFO);
            FileObject subdirLinkFO = baseDirFO.getFileObject(subirLinkName);
            assertNotNull(subdirLinkFO);
            FileObject childFO = baseDirFO.getFileObject(childName);
            assertNotNull(childFO);
            FileObject subdirFO = baseDirFO.getFileObject(subdirName);
            assertNotNull(subdirFO);
            
            subdirFO.getChildren(); // otherwise no file creation event occurs
            subdirLinkFO.getChildren(); // otherwise no file creation event occurs
            
            String prefix = baseDirFO.getPath();
            FileSystemProvider.addRecursiveListener(new DumpingFileChangeListener("recursive", prefix, out, true), baseDirFO.getFileSystem(), baseDirFO.getPath());
            baseDirFO.addFileChangeListener(new DumpingFileChangeListener("baseDir", prefix, out, true));
            subdirFO.addFileChangeListener(new DumpingFileChangeListener(subdirFO.getNameExt(), prefix, out, true));
            subdirLinkFO.addFileChangeListener(new DumpingFileChangeListener(subdirLinkFO.getNameExt(), prefix, out, true));
            
            executeInDir(subdirFO.getPath(), env, "touch",  "file_1.h");
            baseDirFO.refresh();
            
            out.close();
            printFile(log, "REMOTE", System.out);
        } finally {
            if (out != null) {
                out.close();
            }
            removeRemoteDirIfNotNull(baseDir);
        }    
    }
    
    @ForAllEnvironments
    public void testLinkListeners() throws Throwable {
        if (Utilities.isWindows()) {
            System.err.printf("Skipping %s test on Windows\n", getClass().getName());
            return;
        }
        doTestListeners();
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(LinkListenersTestCase.class);
    }
}
