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
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import junit.framework.Test;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class RemoteLinksTestCase extends RemoteFileTestBase {

    public RemoteLinksTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    // The following two methods are copy-paste from org.netbeans.modules.remote.impl.fileoperations.spi.FileOperationsProvider
    // They are used for emulation refresh method used in ADE plugin
    private void refreshFor(String... paths) {
        List<RemoteFileObjectBase> roots = new ArrayList<>();
        for (String path : paths) {
            RemoteFileObjectBase fo = findExistingFileObjectOrParent(path);
            if (fo != null) {
                roots.add(fo);
            }
        }
        for (RemoteFileObjectBase fo : roots) {
            if (fo.isValid()) {
                fo.refresh(true);
            }
        }
    }

    private RemoteFileObjectBase findExistingFileObjectOrParent(String path) {
        while (true) {
            RemoteFileObject fo = RemoteFileSystemManager.getInstance().getFileSystem(execEnv).findResource(path);
            if (fo != null) {
                return fo.getImplementor();
            }
            path = PathUtilities.getDirName(path);
            if (path == null) {
                return null;
            }
        }
    }

    @ForAllEnvironments
    public void testADELinkWorkflow() throws Exception {
        String baseDir = null;

        final String dataFile = "test";

        try {
            baseDir = mkTempAndRefreshParent(true);
            // The following directory structure emulates ADE real one.
            String script =
                    "cd " + baseDir + ";"
                    + "mkdir -p " + baseDir + "/ade_autofs/111/222;"
                    + "echo 123 > " + baseDir + "/ade_autofs/111/222/" + dataFile + ";"
                    + "chmod a-wx " + baseDir + "/ade_autofs/111/222/" + dataFile + ";"
                    + "chmod a+r " + baseDir + "/ade_autofs/111/222/" + dataFile + ";"
                    + "mkdir " + baseDir + "/ade;"
                    + "ln -s " + baseDir + "/ade_autofs/111 " + baseDir + "/ade/111;"
                    + "ln -s " + baseDir + "/ade/111/222 " + baseDir + "/ade_path;"
                    + "ln -s ade_path/" + dataFile + ' ' + dataFile;
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing script \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);

            final FileObject baseDirFO = getFileObject(baseDir);
            assertTrue("FileObject should be readable: " + baseDirFO, baseDirFO.canRead());
            final FileObject dataFileFO = getFileObject(baseDirFO, dataFile);
            int hashCode = dataFileFO.hashCode();
            assertFalse("FileObject should not be writable: " + dataFileFO.getPath(), dataFileFO.canWrite());

            script =
                    "cd " + baseDir + "; "
                    + "mv -f " + baseDir + "/ade_autofs/111/222/" + dataFile + ' ' + dataFile + "; "
                    + "chmod a+w " + dataFile;
            res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing script \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);

            refreshFor(dataFileFO.getPath());
            assertEquals("FileObject hashCode should not change", hashCode, dataFileFO.hashCode());
            assertTrue("FileObject should be writable: " + dataFileFO.getPath(), dataFileFO.canWrite());
            String content = "another brown fox...";
            writeFile(dataFileFO, content);
            CharSequence readContent = readFile(dataFileFO);
            assertEquals("File content differ", content.toString(), readContent.toString());

        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    @ForAllEnvironments
    public void testDirectoryLinkExternalUpdate() throws Exception {
        String baseDir = null;

        final String dataFile = "test";
        final String dirLink = "dir";
        final String content1 = "123";
        final String content2 = "321";

        try {
            baseDir = mkTempAndRefreshParent(true);
            String script =
                    "cd " + baseDir + ";"
                    + "mkdir -p " + baseDir + "/ade_autofs/111;"
                    + "mkdir -p " + baseDir + "/ade_autofs/222;"
                    + "echo " + content1 + " > " + baseDir + "/ade_autofs/111/" + dataFile + ";"
                    + "echo " + content2 + " > " + baseDir + "/ade_autofs/222/" + dataFile + ";"
                    + "ln -s " + baseDir + "/ade_autofs/111 " + baseDir + "/" + dirLink + ';';
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing script \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);

            final FileObject baseDirFO = getFileObject(baseDir);
            final FileObject dirLinkFO = getFileObject(baseDirFO, dirLink);
            final FileObject dataFileFO = getFileObject(dirLinkFO, dataFile);

            assertTrue("FileObject should be readable: " + dataFileFO.getPath(), dataFileFO.canRead());
            CharSequence readContent = readFile(dataFileFO);
            assertEquals("File content differ", content1 + "\n", readContent.toString());

            script =
                    "cd " + baseDir + ";"
                    + "rm " + baseDir + "/" + dirLink + ';'
                    + "ln -s " + baseDir + "/ade_autofs/222 " + baseDir + "/" + dirLink + ';';
            res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing script \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);

            refreshFor(dataFileFO.getPath());
            assertTrue("FileObject should be readable: " + dataFileFO.getPath(), dataFileFO.canRead());
            readContent = readFile(dataFileFO);
            assertEquals("File content differ", content2 + '\n', readContent.toString());

        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    @ForAllEnvironments
    public void testCorrectPaths() throws Exception {
        String baseDir = null;

        final String dataFile = "test";
        final String dirLink = "dir";
        final String content1 = "123";
        final String content2 = "321";

        try {
            baseDir = mkTempAndRefreshParent(true);
            String script =
                    "cd " + baseDir + ";"
                    + "mkdir -p " + baseDir + "/ade_autofs/111;"
                    + "mkdir -p " + baseDir + "/ade_autofs/222;"
//                    + "mkdir -p " + baseDir + "/ade_autofs/222/subdir;"
                    + "echo " + content1 + " > " + baseDir + "/ade_autofs/111/" + dataFile + ";"
                    + "echo " + content2 + " > " + baseDir + "/ade_autofs/222/" + dataFile + ";"
                    + "ln -s " + baseDir + "/ade_autofs/111 " + baseDir + "/" + dirLink + ';';
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing script \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);

            FileObject baseDirFO = getFileObject(baseDir);
            FileObject dirLinkFO = getFileObject(baseDirFO, dirLink);
            FileObject dataFileFO = getFileObject(dirLinkFO, dataFile);
            final String path = dirLinkFO.getPath() + "/" + dataFile;
            assertEquals("child path differs!", dataFileFO.getPath(), path);
            dataFileFO = getFileObject(baseDirFO, dirLink + "/" + dataFile);
            assertEquals("child path differs!", dataFileFO.getPath(), path);
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    private static void doTestIZ_242509(ExecutionEnvironment env) throws Exception {
        String baseDir = null;
        try {
            baseDir = NativeExecutionTestSupport.mkTemp(env, true);
            FileObject granpaFO = FileSystemProvider.getFileObject(env, PathUtilities.getDirName(baseDir));
            assertNotNull(granpaFO);
            granpaFO.refresh();

            String script =
                    "cd " + baseDir + ";"
                    + "mkdir real_dir;"
                    + "ln -s real_dir link_dir;"
                    + "cd real_dir;"
                    + "mkdir -p intel-S2/lib;"
                    + "cd intel-S2/lib;"
                    + "mkdir -p ../LEGAL;"
                    + "echo \"Oracle Solaris Studio 13\" > ../LEGAL/ProductName;"
                    + "ln -s ../LEGAL/ProductName SolarisStudio;";
            ProcessUtils.ExitStatus res = ProcessUtils.execute(env, "sh", "-c", script);
            assertEquals("Error executing script \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);

            final String parentPath = baseDir + "/link_dir/intel-S2/lib";
            final FileObject parentFO = FileSystemProvider.getFileObject(env, parentPath);
            final String refText = "Oracle Solaris Studio 13\n";

            for (String childPath : new String[] { "../LEGAL/ProductName", "SolarisStudio"}) {
                FileObject childFO = parentFO.getFileObject(childPath);
                String actualText = childFO.asText();
                assertEquals("Content for " + childPath, refText, actualText);

//                FileObject parentFO2 = childFO.getParent();
//                System.out.printf("%s\n", childPath);
//                System.out.printf("parent1: %s\n", parentFO);
//                System.out.printf("parent1: %s\n", parentFO);
//                System.out.printf("child:   %s\n", childFO);
//                System.out.printf("parent2: %s\n", parentFO2);
//                System.out.printf("text: %s\n", actualText);
//                System.out.printf("size: %d\n", childFO.getSize());
//                System.out.printf("readFile: %s\n", readFile(childFO));
//                System.out.printf("readFile: %s\n", childFO.asLines().get(0));
            }
        } finally {
            if (baseDir != null) {
                CommonTasksSupport.rmDir(env, baseDir, true, new OutputStreamWriter(System.err)).get();
            }
        }
    }

    @ForAllEnvironments
    public void testIZ_242509() throws Exception {
//        if (Utilities.isUnix()) {
//            doTestIZ_242509(ExecutionEnvironmentFactory.getLocal());
//        }
        doTestIZ_242509(execEnv);
    }

    @ForAllEnvironments
    public void testDirectoryLink() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);

            String realDir = baseDir + "/real_dir";
            String linkDirName = "link_dir";
            String linkDir = baseDir + '/' + linkDirName;
            String realFile = realDir + "/file";
            String linkFile = linkDir + "/file";

            String script =
                    "cd " + baseDir + "; " +
                    "mkdir -p " + realDir + "; " +
                    "ln -s " + realDir + ' ' + linkDirName + "; " +
                    "echo 123 > " + realFile;

            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing script \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);

            final FileObject realFO = getFileObject(realFile);
            final FileObject linkFO = getFileObject(linkFile);

            assertTrue("FileObject should be writable: " + linkFO.getPath(), linkFO.canWrite());
            String content = "a quick brown fox...";
            writeFile(linkFO, content);
            CharSequence readContent = readFile(realFO);
            assertEquals("File content differ", content.toString(), readContent.toString());

            FileObject linkDirFO = getFileObject(linkDir);
            FileObject[] children = linkDirFO.getChildren();
            for (FileObject child : children) {
                String childPath = child.getPath();
                String parentPath = linkDirFO.getPath();
                assertTrue("Incorrect link child path: " + childPath + " should start with parent path " + parentPath,
                        child.getPath().startsWith(parentPath));
            }
            FileObject linkFO2;
            linkFO2 = getFileObject(linkFile);
            assertTrue("Duplicate instances for " + linkFile, linkFO ==linkFO2);
            linkDirFO.refresh();
            linkFO2 = getFileObject(linkFile);
            assertTrue("Duplicate instances for " + linkFile, linkFO ==linkFO2);
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    @ForAllEnvironments
    public void testLinkToItself() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);
            String linkName = "link";
            String script =
                    "cd " + baseDir + "; " +
                    "ln -s " + linkName + ' ' + linkName;
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing script \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);

            FileObject linkFO;

            linkFO = getFileObject(baseDir + '/' + linkName);
            linkFO.canRead();
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    @ForAllEnvironments
    public void testCyclicLinksRefresh() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);
            String selfLinkName = "link";
            String linkName1 = "link1";
            String linkName2 = "link2";
            String baseDirlinkName = "linkToDir";
            String script =
                    "cd " + baseDir + "; " +
                    "ln -s " + selfLinkName + ' ' + selfLinkName + ";" +
                    "ln -s " + linkName1 + ' ' + linkName2 + ";" +
                    "ln -s " + linkName2 + ' ' + linkName1 + ";" +
                    "ln -s " + baseDir + ' ' + baseDirlinkName;
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing script \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);

            FileObject baseDirFO = getFileObject(baseDir);
            baseDirFO.refresh();
            FileObject[] children = baseDirFO.getChildren(); // otherwise existent children are empty => refresh won't cycle
            baseDirFO.refresh();
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    @ForAllEnvironments
    public void testCyclicLinksDelete() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);
            String selfLinkName = "link";
            String linkName1 = "link1";
            String linkName2 = "link2";
            String parentName2 = "link2parent";
            String baseDirlinkName = "linkToDir";
            String cyclickLink1 = "cl1";
            String cyclickLink2 = "cl2";
            String cyclickLink3 = "cl3";
            String script =
                    "cd " + baseDir + "; " +
                    "ln -s " + selfLinkName + ' ' + selfLinkName + ";" +
                    "ln -s " + linkName1 + ' ' + linkName2 + ";" +
                    "ln -s " + linkName2 + ' ' + linkName1 + ";" +
                    "ln -s . " + parentName2 + ";" +
                    "ln -s " + cyclickLink1 + ' ' + cyclickLink2 + ";" +
                    "ln -s " + cyclickLink2 + ' ' + cyclickLink3 + ";" +
                    "ln -s " + cyclickLink3 + ' ' + cyclickLink1 + ";" +
                    "ln -s " + baseDir + ' ' + baseDirlinkName;
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing script \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);
            FileObject baseDirFO = getFileObject(baseDir);
            baseDirFO.refresh();
            FileObject[] children = baseDirFO.getChildren(); // otherwise existent children are empty => refresh won't cycle
            baseDirFO.delete();
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    @ForAllEnvironments
    public void testCyclicLinksNonExistentIsFolder() throws Exception {
        //bz#216212 - StackOverflowError at org.netbeans.modules.remote.impl.fs.RemoteDirectory.getStorageFile
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);
            String folderName = "folder";
            String selfLinkName = "linkToFolder";
            String script =
                    "cd " + baseDir + "; " +
                    "mkdir " + folderName + ";" +
                    "ln -s " + folderName + ' ' + selfLinkName + ";" +
                    " rm -rf " + folderName + ";" +
                    "ln -s " + selfLinkName + ' ' + folderName + ";";
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing script \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);

            FileObject baseDirFO = getFileObject(baseDir);
            baseDirFO.refresh();
            FileObject folderFO = baseDirFO.getFileObject(folderName);
            FileObject linkFO = baseDirFO.getFileObject(selfLinkName);
            assertTrue(!folderFO.canRead());
            assertTrue(folderFO.isData());
            assertTrue(!folderFO.isFolder());
            assertTrue(!linkFO.canRead());
            assertTrue(linkFO.isData());
            assertTrue(!linkFO.isFolder());
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    @ForAllEnvironments
    public void testLinkLastModificationTime() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);
            String linkName = "link";
            String fileName = "data";
            String script =
                    "cd " + baseDir + "; " +
                    "touch " + baseDir + "/" + fileName + ";" +
                    "sleep 10;" +
                    "ln -s " + fileName + ' ' + linkName;
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing script \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);

            FileObject linkFO = getFileObject(baseDir + '/' + linkName);
            FileObject fileFO = getFileObject(baseDir + '/' + fileName);
            assertEquals("Link and it's target modification time should be the same (as with java.io.File)", linkFO.lastModified(), fileFO.lastModified());
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    @ForAllEnvironments
    public void testCreateDataAndFolder() throws Exception {

        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);

            String realDir = baseDir + "/real_dir";
            String linkDirName = "link_dir";
            String linkDir = baseDir + '/' + linkDirName;

            String script =
                    "cd " + baseDir + "; " +
                    "mkdir -p " + realDir + "; " +
                    "ln -s " + realDir + ' ' + linkDirName;

            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing script \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);

            FileObject linkDirFO = getFileObject(linkDir);

            {
                FileObject childData = linkDirFO.createData("child_data");
                assertNotNull(childData);
                assertTrue(childData.isValid());
                assertTrue(childData.isData());
                assertEquals(childData.getParent(), linkDirFO);
                assertTrue(childData.getNameExt().equals("child_data"));
            }
            {
                FileObject childFolder = linkDirFO.createFolder("child_folder");
                assertNotNull(childFolder);
                assertTrue(childFolder.isValid());
                assertTrue(childFolder.isFolder());
                assertEquals(childFolder.getParent(), linkDirFO);
                assertTrue(childFolder.getNameExt().equals("child_folder"));
            }
            {
                FileObject tempFile = rootFO.getFileSystem().createTempFile(linkDirFO, "out", ".tmp", true);
                assertNotNull(tempFile);
                assertTrue(tempFile.isValid());
                assertTrue(tempFile.isData());
                assertEquals(tempFile.getParent(), linkDirFO);
                assertEquals(linkDirFO.getFileSystem(), tempFile.getFileSystem());
                assertTrue(tempFile.getNameExt().startsWith("out"));
                assertTrue(tempFile.getNameExt().endsWith(".tmp"));
            }
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    @ForAllEnvironments
    public void testIZ_258298_disallow_deep_cycles() throws Exception {

        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);
            String[] struct = new String[]{
                "d real_dir_1",
                "- real_dir_1/file_1",
                "d real_dir_1/subdir_1",
                "- real_dir_1/subdir_1/file_2",
                "l ../.. real_dir_1/subdir_1/lnk_up_1",
            };
            createDirStructure(execEnv, baseDir, struct);
            RemoteFileObject baseFO = getFileObject(baseDir);
            baseFO.refresh();
            AtomicInteger cnt = new AtomicInteger();
            System.out.println("### Recursing " + baseFO);
            recurse(baseFO, cnt, 100);
            //System.out.println("### CNT " + cnt.get());
            assertEquals("File objects count", 12, cnt.get());
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    private void recurse(FileObject fo, AtomicInteger cnt, int max) {
        cnt.incrementAndGet();
        if (cnt.get() > max) {
            return;
        }
        for (FileObject child : fo.getChildren()) {
            recurse(child, cnt, max);
        }
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(RemoteLinksTestCase.class);
    }
}
