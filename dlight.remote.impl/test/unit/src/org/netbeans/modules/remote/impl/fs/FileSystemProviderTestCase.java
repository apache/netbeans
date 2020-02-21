/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
