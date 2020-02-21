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

import java.io.OutputStreamWriter;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 */
public class CopyTestCase extends RemoteFileTestBase  {

    public CopyTestCase(String testName) {
        super(testName);
    }

    public CopyTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testCopyPlainFile() throws Exception {
        String tempDir = null;
        try {
            tempDir = mkTempAndRefreshParent(true);
            FileObject tempDirFO = getFileObject(tempDir);
            FileObject subDirFO = FileUtil.createFolder(tempDirFO, "subdir_1");            
            FileObject src = FileUtil.createData(tempDirFO, "file_1");
            String refText = "A quick brown fox jumps over the lazy dog";
            writeFile(src, refText);
            FileObject copy = src.copy(subDirFO, "file_1_copy", "");
            String text = readFile(copy);
            assertEquals("content of " + copy.getPath(), refText, text);
        } finally {
            if (tempDir != null) {
                CommonTasksSupport.rmDir(execEnv, tempDir, true, new OutputStreamWriter(System.err));
            }
        }
    }

    @ForAllEnvironments
    public void testCopyLinkToPlainFile() throws Exception {
        String tempDir = null;
        try {
            tempDir = mkTempAndRefreshParent(true);
            FileObject tempDirFO = getFileObject(tempDir);
            FileObject subDirFO = FileUtil.createFolder(tempDirFO, "subdir_1");            
            FileObject orig = FileUtil.createData(tempDirFO, "file_1");
            String lnkName = "link_1";
            String refText = "A quick brown fox jumps over the lazy dog";
            writeFile(orig, refText);
            runScript("cd " + tempDir + ';' + " ln -s " + orig.getPath() + ' ' + lnkName + '\n');
            tempDirFO.refresh();
            FileObject src = tempDirFO.getFileObject(lnkName);
            assertTrue(src.getPath() + " should be a link", FileSystemProvider.isLink(src));
            assertNotNull("File object for link - " + lnkName, src);           
            FileObject copy = src.copy(subDirFO, "link_1_copy", "");
            String text = readFile(copy);
            assertEquals("content of " + copy.getPath(), refText, text);
            assertTrue(copy.getPath() + " should be a link", FileSystemProvider.isLink(copy));
        } finally {
            if (tempDir != null) {
                CommonTasksSupport.rmDir(execEnv, tempDir, true, new OutputStreamWriter(System.err));
            }
        }
    }

    @ForAllEnvironments
    public void testCopyDirSimple() throws Exception {
        String tempDir = null;
        try {
            tempDir = mkTempAndRefreshParent(true);
            final FileObject tempDirFO = getFileObject(tempDir);
            final FileObject subDirFO1 = FileUtil.createFolder(tempDirFO, "subdir_1");
            final FileObject plainSrc = FileUtil.createData(subDirFO1, "file_1");
            final String refText = "A quick brown fox jumps over the lazy dog";
            final String absLinkName = "abs_link";
            final String relLinkName = "rel_link";
            writeFile(plainSrc, refText);
            runScript("cd " + subDirFO1.getPath() + '\n' + 
                    " ln -s " + plainSrc.getPath() + ' ' + absLinkName + "\n" +
                    " ln -s " + plainSrc.getNameExt() + ' ' + relLinkName + "\n" +
                    '\n');
            tempDirFO.refresh();
            FileObject subdirCopy = subDirFO1.copy(tempDirFO, "subdir_1_copy", "");
            FileObject plainCopy = subdirCopy.getFileObject(plainSrc.getNameExt());
            assertNotNull(plainCopy);            
            String text = readFile(plainCopy);
            assertEquals("content of " + subdirCopy.getPath(), refText, text);
        } finally {
            if (tempDir != null) {
                CommonTasksSupport.rmDir(execEnv, tempDir, true, new OutputStreamWriter(System.err));
            }
        }
    }

    
    public static Test suite() {
        return RemoteApiTest.createSuite(CopyTestCase.class);
    }    
}
