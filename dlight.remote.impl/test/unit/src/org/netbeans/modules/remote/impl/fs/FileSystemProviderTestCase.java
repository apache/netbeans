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

import junit.framework.Test;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class FileSystemProviderTestCase extends RemoteFileTestBase {

    public FileSystemProviderTestCase(String testName) {
        super(testName);
    }
    
    public FileSystemProviderTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testIsLink() throws Exception {

        String path = "/usr/include/stdio.h";
        FileObject fo = getFileObject(path);
        assertFalse("Should not be a link: " + fo, FileSystemProvider.isLink(fo));
        assertFalse("Should not be a link: " + fo, FileSystemProvider.isLink(getTestExecutionEnvironment(), fo.getPath()));
        assertFalse("Should not be a link: " + fo, FileSystemProvider.isLink(fo.getFileSystem(), fo.getPath()));
        
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);

            String fileName = "real_file";
            String relLinkName1 = "relative_link_1";
            String relLinkName2 = "relative_link_2";
            String absLinkName = "absolute_link";
            String brokenLinkName = "inexistent_link";
            String script = 
                    "cd " + baseDir + "; " +
                    "rm -rf *; " +
                    "touch " + fileName + "; " +                    
                    "ln -s " + fileName + ' ' +  relLinkName1 + "; " +                    
                    "ln -s ../" + PathUtilities.getBaseName(baseDir) + '/' + fileName + ' ' +  relLinkName2 + "; " +                    
                    "ln -s " + baseDir + '/' + fileName + ' ' +  absLinkName + "; " +                    
                    "ln -s abrakadabra " +  brokenLinkName + "; ";

            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing script \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);

            checkLink(baseDir, fileName, false);
            checkLink(baseDir, relLinkName1, true);
            checkLink(baseDir, relLinkName2, true);
            checkLink(baseDir, absLinkName, true);
            checkLink(baseDir, brokenLinkName, true);
            
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }        
    }

    @ForAllEnvironments
    public void testResolveLink() throws Exception {

        final ExecutionEnvironment env = getTestExecutionEnvironment();

        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);

            String fileName = "real_file";
            String relLinkName1 = "relative_link_1";
            String relLinkName2 = "relative_link_2";
            String absLinkName = "absolute_link";
            String brokenLinkName = "inexistent_link";
            String script =
                    "cd " + baseDir + "; " +
                    "rm -rf *; " +
                    "touch " + fileName + "; " +
                    "ln -s " + fileName + ' ' +  relLinkName1 + "; " +
                    "ln -s ../" + PathUtilities.getBaseName(baseDir) + '/' + fileName + ' ' +  relLinkName2 + "; " +
                    "ln -s " + baseDir + '/' + fileName + ' ' +  absLinkName + "; " +
                    "ln -s abrakadabra " +  brokenLinkName + "; ";

            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing script \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);

            checkResolveLink(baseDir, fileName, null);
            checkResolveLink(baseDir, relLinkName1, baseDir + '/' + fileName);
            checkResolveLink(baseDir, relLinkName2, baseDir + '/' + fileName);
            checkResolveLink(baseDir, absLinkName, baseDir + '/' + fileName);
            checkResolveLink(baseDir, brokenLinkName, baseDir + "/abrakadabra");

        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    private void checkLink(String baseDir, String path, boolean link) throws Exception {
        if (!path.startsWith("/")) {
            path = baseDir + '/' + path;
        }
        final ExecutionEnvironment env = getTestExecutionEnvironment();
        if (link) {
            assertTrue("Should be a link: " + path, FileSystemProvider.isLink(env, path));
        } else {
            assertFalse("Should not be a link: " + path, FileSystemProvider.isLink(env, path));
        }
    }


    private void checkResolveLink(String baseDir, String path, String expected) throws Exception {
        if (!path.startsWith("/")) {
            path = baseDir + '/' + path;
        }
        FileObject fo = getFileObject(path);
        final String resolvedLink = FileSystemProvider.resolveLink(fo);
        assertEquals("resolveLink for " + path, expected, resolvedLink);
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(FileSystemProviderTestCase.class);
    }

}
