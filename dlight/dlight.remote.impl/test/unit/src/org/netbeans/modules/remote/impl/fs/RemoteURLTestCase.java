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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import junit.framework.Test;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 *
 */
public class RemoteURLTestCase extends RemoteFileTestBase {

    public RemoteURLTestCase(String testName) {
        super(testName);
    }
    
    public RemoteURLTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testUrlToFileObjectParity() throws Exception {
        for (int i = 0; i < 2; i++) {
            String path = "/usr/include/stdio.h";
            FileObject fo = getFileObject(path);
            URL url = fo.toURL();
            String str;
            if (i == 0) {
                str = PathUtilities.unescapePath(url.toExternalForm());
            } else {
                str = url.toExternalForm();
            }
            FileObject fo2 = FileSystemProvider.urlToFileObject(str);
            assertNotNull(fo2);
            assertEquals("File Object ", fo, fo2);
            str = FileSystemProvider.fileObjectToUrl(fo);
            fo2 = FileSystemProvider.urlToFileObject(str);
            assertNotNull(fo2);
            assertEquals("File Object ", fo, fo2);
        }
    }
    
    @ForAllEnvironments
    public void testUrlToFileObject() throws Exception {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        String path = "/usr/include/stdio.h";
        String url;
        FileObject fo;
        
        url = RemoteFileURLStreamHandler.PROTOCOL_PREFIX + env.getHost() + ':' + env.getSSHPort() + ':' + path;
        fo = FileSystemProvider.urlToFileObject(url);
        assertNotNull("Null file object for " + url, fo);
        if (env.getSSHPort() == 22) {
            url = RemoteFileURLStreamHandler.PROTOCOL_PREFIX + env.getHost() + ':' + path;
            fo = FileSystemProvider.urlToFileObject(url);
            assertNotNull("Null file object for " + url, fo);
        }
                 
        url = RemoteFileURLStreamHandler.PROTOCOL_PREFIX + env.getUser() + '@' + env.getHost() + ':' + env.getSSHPort()  + ':' + path;
        fo = FileSystemProvider.urlToFileObject(url);
        assertNotNull("Null file object for " + url, fo);
        if (env.getSSHPort() == 22) {
            url = RemoteFileURLStreamHandler.PROTOCOL_PREFIX + env.getUser() + '@' + env.getHost() + ':' + path;
            fo = FileSystemProvider.urlToFileObject(url);
            assertNotNull("Null file object for " + url, fo);
        }
    }
    
    @ForAllEnvironments
    public void testFindURL() throws Exception {
        String absPath = "/usr/include/stdio.h";
        FileObject fo = getFileObject(absPath);
        URL url = URLMapper.findURL(fo, URLMapper.EXTERNAL);
        assertNotNull("null URL", url);
        String ext = url.toExternalForm();
        URL fromExt = new URL(ext);
        assertEquals("Url restored from external form, differs", url, fromExt);
    }

    @ForAllEnvironments
    public void testURLtoFileObject() throws Exception {
        String absPath = "/usr/include/stdlib.h";
        FileObject fo = getFileObject(absPath);
        URL url = URLMapper.findURL(fo, URLMapper.EXTERNAL);
        assertNotNull("null URL", url);
        FileObject fo2 = URLMapper.findFileObject(url);
        assertNotNull("null file object by URL " + url, fo2);
        assertEquals("file objects differ", fo, fo2);
    }

    @ForAllEnvironments
    public void testURLConnectionRead() throws Exception {
        String tempFile = null;
        try {
            tempFile = mkTempAndRefreshParent();
            FileObject fo = getFileObject(tempFile);
            assertTrue("FileObject should be readable: " + fo.getPath(), fo.canRead());
            final String referenceText = "a quick brown fox...";
            writeFile(fo, referenceText);
            String readContent = ProcessUtils.execute(execEnv, "cat", tempFile).getOutputString();
            assertEquals("File content differ", referenceText.toString(), readContent.toString());
            doTestUrlConnectionRead(fo, readContent, true);
            doTestUrlConnectionRead(fo, readContent, false);
        } finally {
            if (tempFile != null) {
                removeRemoteDirIfNotNull(tempFile);
            }
        }
    }

    private void doTestUrlConnectionRead(FileObject fo, String referenceContent, boolean connect) throws IOException {
        URL url = URLMapper.findURL(fo, URLMapper.EXTERNAL);
        assertNotNull("null URL", url);
        URLConnection conn = url.openConnection();
        assertNotNull("null URLConnection", conn);
        if (connect) {
            conn.connect();
        }
        BufferedReader rdr = null;
        try {
            rdr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = rdr.readLine();
            assertEquals(referenceContent, line);
        } finally {
            if (rdr != null) {
                rdr.close();
            }
        }
    }

    @ForAllEnvironments
    public void testURLConnectionWrite() throws Exception {
        String tempFile = null;
        try {
            tempFile = mkTempAndRefreshParent();
            final String referenceText = "...jumps over a lazy dog";
            FileObject fo = getFileObject(tempFile);
            URL url = URLMapper.findURL(fo, URLMapper.EXTERNAL);
            assertNotNull("null URL", url);
            URLConnection conn = url.openConnection();
            assertNotNull("null URLConnection", conn);
            conn.connect();
            Writer wr = null;
            try {
                wr = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
                wr.append(referenceText);
            } finally {
                if (wr != null) {
                    wr.close();
                }
            }
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "cat", tempFile);
            assertEquals("cat failed: " + res.getErrorString(), 0, res.exitCode);
            assertEquals(referenceText, res.getOutputString());
        } finally {
            if (tempFile != null) {
                removeRemoteDirIfNotNull(tempFile);
            }
        }
    }

    @ForAllEnvironments
    public void testUrlForPathWithASharp() throws Exception {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        String remoteBaseDir = null;
        try {
            remoteBaseDir = mkTempAndRefreshParent(true);
            FileObject remoteBaseDirFO = getFileObject(remoteBaseDir);
            FileObject fo = remoteBaseDirFO.createData("path#with#a#sharp");
            assertNotNull(fo);
            URL url = fo.toURL();
            URI uri = fo.toURI();
            FileObject fo2 = URLMapper.findFileObject(url);
            assertEquals("File objects should be equal", fo, fo2);
            //assertTrue("File objects should be the same instance: " + fo + " and " + fo2, fo == fo2);
        } finally {
            removeRemoteDirIfNotNull(remoteBaseDir);
        }
    }

    @ForAllEnvironments
    public void testUrlForPathWithASpace() throws Exception {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        String remoteBaseDir = null;
        try {
            remoteBaseDir = mkTempAndRefreshParent(true);
            FileObject remoteBaseDirFO = getFileObject(remoteBaseDir);
            FileObject fo = remoteBaseDirFO.createData("path with a space");
            assertNotNull(fo);
            URL url = fo.toURL();
            URI uri = fo.toURI();
            FileObject fo2 = URLMapper.findFileObject(url);
            assertEquals("File objects should be equal", fo, fo2);
            //assertTrue("File objects should be the same instance: " + fo + " and " + fo2, fo == fo2);
        } finally {
            removeRemoteDirIfNotNull(remoteBaseDir);
        }
    }
    
    private void doTestPathWithSpecialCharacters(char[] chars) throws Exception {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        String remoteBaseDir = null;
        try {
            remoteBaseDir = mkTempAndRefreshParent(true);
            FileObject remoteBaseDirFO = getFileObject(remoteBaseDir);
            for (char c : chars) {
                if (c == '\n' || c == '\r'  || c == '"' || c == '\'' 
                        || c == '$' || c == '\\' || c == '`') {
                    // We fail to create such files so far.
                    // That's because we use "touch" command when creating a plain file.
                    // We could use fs_server for plain file creation, then we would be able to do this.
                    // The querstion is whether it's worth doing that :)
                    continue;
                }
                String name = "file" + c + "with" + c;
                FileObject fo = remoteBaseDirFO.createData(name);
                assertNotNull(fo);
                URL url = fo.toURL();
                try {
                    URI uri = fo.toURI();
                } catch (Exception ex) {
                    throw new Exception("Error converting path with '" + c + "' to URI", ex);
                }                
                FileObject fo2 = URLMapper.findFileObject(url);
                assertEquals("File objects should be equal", fo, fo2);
                //assertTrue("File objects should be the same instance: " + fo + " and " + fo2, fo == fo2);
            }
        } finally {
            removeRemoteDirIfNotNull(remoteBaseDir);
        }
    }
    
    @ForAllEnvironments
    public void testUrlForPathWithReserved() throws Exception {
        doTestPathWithSpecialCharacters(new char[] {
            ';', /*'/',*/ '?', ':', '@', '&', '=', '+', '$', ','
        });
    }

    @ForAllEnvironments
    public void testUrlForPathWithMark() throws Exception {
        doTestPathWithSpecialCharacters(new char[] {
            '-', '_', '.', '!', '~', '*', '\'', '(', ')'
        });
    }
    
    @ForAllEnvironments
    public void testUrlForPathWithDelims() throws Exception {
        doTestPathWithSpecialCharacters(new char[] {
            '<', '>', '#', '%', '"'
        });
    }
    
    @ForAllEnvironments
    public void testUrlForPathWithUnwise() throws Exception {
        doTestPathWithSpecialCharacters(new char[] {
            '{', '}', '|', '\\', '^', '[', ']', '`'
        });
    }
    
    @ForAllEnvironments
    public void testUrlForPathWithControl() throws Exception {
        doTestPathWithSpecialCharacters(new char[] {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF,
            0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
            0x7F
        });
    }
    
//    @ForAllEnvironments
//    public void testNewFile() throws Exception {
//        String absPath = "/usr/include/stdio.h";
//        FileObject fo = rootFO.getFileObject(absPath);
//        assertNotNull("Null file object for " + getFileName(execEnv, absPath), fo);
//        URL url = URLMapper.findURL(fo, URLMapper.EXTERNAL);
//        assertNotNull("null URL", url);
//        URI uri = url.toURI();
//        assertNotNull("null URI", uri);
//        File file = new File(uri); // throws IllegalArgumentException: URI scheme is not "file"
//    }

//    @ForAllEnvironments
//    public void testToFile() throws Exception {
//        String absPath = "/usr/include/stdio.h";
//        FileObject fo = rootFO.getFileObject(absPath);
//        assertNotNull("Null file object for " + getFileName(execEnv, absPath), fo);
//        URL url = URLMapper.findURL(fo, URLMapper.EXTERNAL);
//        assertNotNull("null URL", url);
//        File file = FileUtil.toFile(fo);
//        assertNotNull("null file", file);
//    }

    public static Test suite() {
        return RemoteApiTest.createSuite(RemoteURLTestCase.class);
    }

}
