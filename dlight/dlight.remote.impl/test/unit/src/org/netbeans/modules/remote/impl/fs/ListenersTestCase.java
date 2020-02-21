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
import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class ListenersTestCase extends RemoteFileTestBase {

    public ListenersTestCase(String testName) {
        super(testName);
    }

    
    public ListenersTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }
    
    private class FCL implements FileChangeListener {

        private final String listenerName;
        private final Map<FileObject, FileEvent> map;

        public FCL(String name, Map<FileObject, FileEvent> map) {
            this.listenerName = name;
            this.map = map;
        }

        private void register(String eventKind, FileEvent fe) {
            if (map != null) {
                map.put(fe.getFile(), fe);
            }
            if (RemoteApiTest.TRACE_LISTENERS) {
                System.out.printf("FileEvent[%s]: %s %s\n", listenerName, eventKind, fe);
            }
        }
        
         @Override
       public void fileAttributeChanged(FileAttributeEvent fe) {
            register("fileAttributeChanged", fe);
        }

         @Override
       public void fileChanged(FileEvent fe) {
            register("fileChanged", fe);
        }

         @Override
       public void fileDataCreated(FileEvent fe) {
            register("fileDataCreated", fe);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            register("fileDeleted", fe);
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            register("fileFolderCreated", fe);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            register("fileRenamed", fe);
        }        
    }

//    public void testLocalListeners() throws Exception {
//        File file = File.createTempFile("listeners-test", ".dat");
//        file.delete();
//        file.mkdirs();
//        FileObject baseDirFO = FileUtil.toFileObject(file);        
//        baseDirFO.getFileSystem().addFileChangeListener(new FCL("FS", null));
//        baseDirFO.addFileChangeListener(new FCL("baseDir", null));
//        FileObject childFO = baseDirFO.createData("child_file_1");
//        FileObject subdirFO = baseDirFO.createFolder("child_folder");
//        subdirFO.addFileChangeListener(new FCL(subdirFO.getNameExt(), null));
//        FileObject grandChildFO = subdirFO.createData("grand_child_file");
//        FileObject grandChildDirFO = subdirFO.createFolder("grand_child_dir");
//        FileObject grandGrandChildFO = grandChildDirFO.createData("grand_grand_child_file");
//    }

    enum Kind {
        ORDINARY,
        RECURSIVE,
        FILESYSTEM,
        GLOBAL
    }
    private void doTestListeners(Kind kind) throws Exception {
        String baseDir = null;
        try {          
            baseDir = mkTempAndRefreshParent(true);
            Map<FileObject, FileEvent> evMap = new HashMap<>();
            FileObject baseDirFO = getFileObject(baseDir);
            FCL fcl = new FCL("baseDir", evMap);
            switch (kind) {
                case RECURSIVE:
                    FileSystemProvider.addRecursiveListener(fcl, fs, baseDir);
                    break;
                case ORDINARY:
                    baseDirFO.addFileChangeListener(fcl);
                    break;
                case FILESYSTEM:
                    fs.addFileChangeListener(fcl);
                    break;
                case GLOBAL:
                    FileSystemProvider.addFileChangeListener(fcl);
                    break;
            }
            FileObject childFO = baseDirFO.createData("child_file_1");
            FileObject subdirFO = baseDirFO.createFolder("child_folder");
            if (kind == Kind.ORDINARY) {
                subdirFO.addFileChangeListener(new FCL(subdirFO.getNameExt(), evMap));
            }
            FileObject grandChildFO = subdirFO.createData("grand_child_file");
            FileObject grandChildDirFO = subdirFO.createFolder("grand_child_dir");
            FileObject grandGrandChildFO = grandChildDirFO.createData("grand_grand_child_file");
            FileEvent fe;
            fe = evMap.get(childFO);
            assertNotNull("No file event for " + childFO, fe);
            fe = evMap.get(subdirFO);
            assertNotNull("No file event for " + childFO, fe);
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }
    
    @ForAllEnvironments
    public void testPendingListeners() throws Exception {
        String baseDir = null;
        try {          
            baseDir = mkTempAndRefreshParent(true);
            Map<FileObject, FileEvent> evMap = new HashMap<>();
            FileObject baseDirFO = getFileObject(baseDir);
            FCL fcl = new FCL("baseDir", evMap);
            baseDirFO.addFileChangeListener(fcl);
            String childName = "child_file_1";
            assertNull("Should be null", baseDirFO.getFileObject(childName));
            String childPath = baseDirFO.getPath() + '/' + childName;
            FileSystemProvider.addFileChangeListener(new FCL(childPath, evMap), execEnv, childPath);
            FileObject childFO = baseDirFO.createData(childName);            
            FileEvent fe;
            fe = evMap.get(childFO);
            assertNotNull("No file event for " + childFO, fe);
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }        
    }

    @ForAllEnvironments
    public void testListeners() throws Exception {
        doTestListeners(Kind.ORDINARY);
    }

    @ForAllEnvironments
    public void testRecursiveListeners() throws Exception {
        doTestListeners(Kind.RECURSIVE);
    }
           
    @ForAllEnvironments
    public void testFileSystemListeners() throws Exception {
        doTestListeners(Kind.FILESYSTEM);
    }
           
    @ForAllEnvironments
    public void testGlobalListeners() throws Exception {
        doTestListeners(Kind.GLOBAL);
    }

    @ForAllEnvironments
    public void testFileExternalChange() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);

            FileObject baseDirFO = getFileObject(baseDir);
            FileObject childFO = baseDirFO.createData("child_file_1");

            Map<FileObject, FileEvent> childMap = new HashMap<>();
            Map<FileObject, FileEvent> parentMap = new HashMap<>();

            baseDirFO.addFileChangeListener(new FCL("Dir", parentMap));
            childFO.addFileChangeListener(new FCL("File", childMap));

            ProcessUtils.ExitStatus rc = ProcessUtils.execute(getTestExecutionEnvironment(),
                    "/bin/sh", "-c", "echo new_content > " + childFO.getPath());
            assertTrue("external modification command failed", rc.exitCode == 0);
            baseDirFO.refresh();

            FileEvent fe;

            fe = childMap.get(childFO);
            assertNotNull("No file event fired for child " + childFO, fe);

            fe = parentMap.get(childFO);
            assertNotNull("No file event for parent " + childFO, fe);
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(ListenersTestCase.class);
    }
}
