/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
