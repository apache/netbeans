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
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class RemoteFileSystemTestCase extends RemoteFileTestBase {

    static { 
//        System.setProperty("remote.fs_server", "true");
//        System.setProperty("remote.fs_server.verbose", "0");
//        System.setProperty("remote.fs_server.log", "true");
//        System.setProperty("remote.fs_server.verbose.response", "true");
//        System.setProperty("remote.fs_server.refresh", "1");
//        RemoteLogger.getInstance().setLevel(Level.FINEST);
    }

    public RemoteFileSystemTestCase(String testName) {
        super(testName);
    }
    
    public RemoteFileSystemTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testRemoteStdioH() throws Exception {
        String absPath = "/usr/include/stdio.h";
        FileObject fo = getFileObject(absPath);
        assertTrue("File " +  getFileName(execEnv, absPath) + " does not exist", fo.isValid());
        String content = readFile(fo);
        String text2search = "printf";
        assertTrue("Can not find \"" + text2search + "\" in " + getFileName(execEnv, absPath),
                content.indexOf(text2search) >= 0);
    }
    
    @ForAllEnvironments
    public void testParents() throws Exception {
        String absPath = "/usr/include/sys/time.h";
        FileObject fo = getFileObject(absPath);
        FileObject p = getParentAssertNotNull(fo); // /usr/include/sys
        p = getParentAssertNotNull(p); // /usr/include
        p = getParentAssertNotNull(p); // /usr
        p = getParentAssertNotNull(p); // /
        assertTrue(p == rootFO);
    }

    private FileObject getParentAssertNotNull(FileObject fo) {
        FileObject parent = fo.getParent(); // /usr/include
        assertNotNull("Null parent for " + fo, parent);
        return parent;
    }

    @ForAllEnvironments
    public void testDifferentPaths() throws Exception {
        class Pair {
            final String parent;
            final String relative;
            public Pair(String parent, String relative) {
                this.parent = parent;
                this.relative = relative;
            }
        }
        Pair[] pairs = new Pair[] {
            new Pair(null, "/usr/include/stdlib.h"),
            new Pair("/usr", "include/stdlib.h"),
            new Pair("/usr/include", "stdlib.h"),            
//            new Pair("/usr/lib", "libc" + sharedLibExt),
//            new Pair("/usr", "lib/libc" + sharedLibExt),
//            new Pair(null, "/usr/lib/libc" + sharedLibExt)
        };
        for (int i = 0; i < pairs.length; i++) {
            Pair pair = pairs[i];
            FileObject parentFO;
            if (pair.parent == null) {
                parentFO = rootFO;
            } else {
                parentFO = getFileObject(pair.parent);
            }
            FileObject childFO = getFileObject(parentFO, pair.relative);
        }
    }

    @ForAllEnvironments
    public void testSingleFileObject() throws Exception {
        String absPath = "/usr/include/stdio.h";
        FileObject fo1 = getFileObject(absPath);
        assertTrue("File " +  getFileName(execEnv, absPath) + " does not exist", fo1.isValid());
        FileObject fo2 = getFileObject(absPath);
        assertTrue("File " +  getFileName(execEnv, absPath) + " does not exist", fo2.isValid());
        assertTrue("Two instances of file objects for " + absPath, fo1 == fo2);
    }

    @ForAllEnvironments
    public void testMultipleRead() throws Exception {
        removeDirectory(fs.getCache());
        final String absPath = "/usr/include/errno.h";
        long firstTime = -1;
        for (int i = 0; i < 5; i++) {
            long time = System.currentTimeMillis();
            FileObject fo = getFileObject(absPath);
            assertTrue("File " +  getFileName(execEnv, absPath) + " does not exist", fo.isValid());
            InputStream is = fo.getInputStream();
            assertNotNull("Got null input stream for " + getFileName(execEnv, absPath), is);
            is.close();
            time = System.currentTimeMillis() - time;
            System.err.printf("Pass %d; getting input stream for %s took %d ms\n", i, getFileName(execEnv, absPath), time);
            if (i == 0) {
                firstTime = time;
            } else if (time > 0) {
                assertTrue("Getting input stream for "+ getFileName(execEnv, absPath) + "(pass " + (i+1) + ")_ took too long (" +
                        time + ") ms (vs" + firstTime + " ms on 1-st pass", time <= firstTime / 8);
            }
        }
    }

    @ForAllEnvironments
    public void testInexistance() throws Exception {
        String path = "/dev/qwe/asd/zxc";
        FileObject fo = rootFO.getFileObject(path);
        assertTrue("File " + getFileName(execEnv, path) + " does not exist, but is reported as existent",
                fo == null || !fo.isValid());
    }

    @ForAllEnvironments
    public void testWrite() throws Exception {
        String tempFile = null;
        try {
            FileObject fo;
            String stdio_h = "/usr/include/stdio.h";
            fo = getFileObject(stdio_h);
            assertFalse("FileObject should NOT be writable: " + fo.getPath(), fo.canWrite());
            tempFile = mkTempAndRefreshParent();
            fo = getFileObject(tempFile);
            assertTrue("FileObject should be writable: " + fo.getPath(), fo.canWrite());
            String content = "a quick brown fox...";
            writeFile(fo, content);
            CharSequence readContent = readFile(fo);
            assertEquals("File content differ", content.toString(), readContent.toString());
            readContent = ProcessUtils.execute(execEnv, "cat", tempFile).getOutputString();
            assertEquals("File content differ", content.toString(), readContent.toString());
        } finally {
            if (tempFile != null) {
                removeRemoteDirIfNotNull(tempFile);
            }
        }
    }

    @ForAllEnvironments
    public void testCanReadWriteExecute() throws Exception {
        String tempFile = null;
        try {
            String stdlib_h = "/usr/include/stdlib.h";
            FileObject stdlib_fo = getFileObject(stdlib_h);
            assertFalse("FileObject should NOT be writable: " + stdlib_fo.getPath(), stdlib_fo.canWrite());
            assertTrue("FileObject should be readable: " + stdlib_fo.getPath(), stdlib_fo.canRead());
            assertFalse("FileObject should NOT be executable: " + stdlib_fo.getPath(), FileSystemProvider.canExecute(stdlib_fo));            
            tempFile = mkTempAndRefreshParent();
            FileObject temp_fo = getFileObject(tempFile);
            assertTrue("FileObject should be readable: " + temp_fo.getPath(), temp_fo.canRead());
            assertTrue("FileObject should be writable: " + temp_fo.getPath(), temp_fo.canWrite());
            assertFalse("FileObject should NOT be executable: " + temp_fo.getPath(), FileSystemProvider.canExecute(temp_fo));                        
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "chmod", "u+x", temp_fo.getPath());
            assertEquals("chmod failed: " + res.getErrorString(), 0, res.exitCode);
            temp_fo.getParent().refresh();
            assertTrue("FileObject should be executable: " + temp_fo.getPath(), FileSystemProvider.canExecute(temp_fo));
            
        } finally {
            if (tempFile != null) {
                removeRemoteDirIfNotNull(tempFile);
            }
        }
    }
    
    @ForAllEnvironments
    public void testReservedWindowsNames() throws Exception {
        String tempDir = null;
        try {
            tempDir = mkTempAndRefreshParent(true);
            FileObject tempDirFO = getFileObject(tempDir);
            //assertTrue("FileObject should be writable: " + tempDirFO.getPath(), tempDirFO.canWrite());
            String lpt = "LPT1";
            String withColon = "file:with:colon";
            runScript("cd " + tempDir + "\n" +
                "echo \"123\" > " + lpt + "\n" +
                "echo \"123\" > " + withColon + "\n");
            tempDirFO.refresh();
            FileObject lptFO = getFileObject(tempDirFO, lpt);
            FileObject colonFO = getFileObject(tempDirFO, withColon);
        } finally {
            if (tempDir != null) {
                CommonTasksSupport.rmDir(execEnv, tempDir, true, new OutputStreamWriter(System.err));
            }
        }
    }
    
    @ForAllEnvironments
    public void testReservedRfsNames() throws Exception {
        String tempDir = null;
        try {
            tempDir = mkTempAndRefreshParent(true);
            FileObject tempDirFO = getFileObject(tempDir);
            //assertTrue("FileObject should be writable: " + tempDirFO.getPath(), tempDirFO.canWrite());
            String resName1 = RemoteFileSystem.CACHE_FILE_NAME;
            String resName2 = RemoteFileSystem.ATTRIBUTES_FILE_NAME;
            String refText = "QWE";
            runScript("cd " + tempDir + "\n" +
                "echo \"" + refText + "\" > " + resName1 + "\n" +
                "echo \"" + refText + "\" > " + resName2 + "\n");
            FileObject fo1 = getFileObject(tempDirFO, resName1);
            FileObject fo2 = getFileObject(tempDirFO, resName2);
            String text1 = readFile(fo1);
            assertEquals("content of " + fo1.getPath(), refText + "\n", text1);
            String text2 = readFile(fo1);
            assertEquals("content of " + fo2.getPath(), refText + "\n", text2);
        } finally {
            if (tempDir != null) {
                CommonTasksSupport.rmDir(execEnv, tempDir, true, new OutputStreamWriter(System.err));
            }
        }
    }    
    
    @ForAllEnvironments
    public void testDate() throws Exception {
        String path = null;
        try {
            path = mkTempAndRefreshParent();
            Date localDate = new Date();
            FileObject fo = getFileObject(path);
            assertTrue("Invalid file object " + path, fo.isValid());
            Date lastMod = fo.lastModified();
            assertNotNull("getDate() returned null for " + fo, lastMod);
            System.out.println("local file creation date:  " + localDate);
            System.out.println("remote last modified date: " + lastMod);

            long skew = HostInfoUtils.getHostInfo(getTestExecutionEnvironment()).getClockSkew();
            long delta = Math.abs(localDate.getTime() - lastMod.getTime());
            if (delta > Math.abs(skew) + (long)(1000*60*15)) {
                assertTrue("Dates differ to much for " + fo + ": " + localDate +  " vs " + lastMod + 
                        " delta " + delta + " ms; skew " + skew, false);
            }
            fo.delete();
            assertTrue("isValid should return false for " + fo, !fo.isValid());
            Date lastMod2 = fo.lastModified();
            System.out.println("remote date after deletion: " + lastMod2);
            assertNotNull("getDate() should never return null", lastMod2);
        } finally {
            if (path != null) {
                removeRemoteDirIfNotNull(path);
            }
        }
    }

    @ForAllEnvironments
    public void testDots() throws Exception {
        FileObject usr = getFileObject("/usr");
        FileObject usrInclude = getFileObject("/usr/include");
        FileObject usrIncludeSys = getFileObject("/usr/include/sys");
        FileObject fo;
        
        for (String relPath : new String[] { ".", "./." }) {
            fo = getFileObject(usr, relPath);
            assertSameInstance("getFileObject(\"" + relPath + "\")", usr, fo);
        }
        
        for (String relPath : new String[] { "..", "./..", "./../" }) {
            fo = getFileObject(usrInclude, relPath);
            assertSameInstance("getFileObject(\"" + relPath + "\")", usr, fo);
        }
        
        for (String relPath : new String[] { "../..", "./../..", "./../../" }) {
            fo = getFileObject(usrIncludeSys, relPath);
            assertSameInstance("getFileObject(\"" + relPath + "\")", usr, fo);
        }
    }
    
    private void assertSameInstance(String message, Object expected, Object actual) {
        assertTrue(message + " expected " + expected + ", but was " + actual, expected == actual);
    }
    
    
    public static Test suite() {
        return RemoteApiTest.createSuite(RemoteFileSystemTestCase.class);
    }
}
