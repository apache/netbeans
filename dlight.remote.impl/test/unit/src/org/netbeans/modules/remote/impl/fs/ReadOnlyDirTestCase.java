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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.impl.fs;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class ReadOnlyDirTestCase extends RemoteFileTestBase {

    public ReadOnlyDirTestCase(String testName) {
        super(testName);
    }
    
    public ReadOnlyDirTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }


    @ForAllEnvironments
    public void testReadOnlyDirectory() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);
            String roDirName = "ro_dir";
            String rwDirName = "rw_sub_dir";
            String fileName1 = "file_1";
            String fileName2 = "file_2";
            String roDirPath = baseDir + '/' + roDirName;
            String rwDirPath = roDirPath + '/' + rwDirName;
            String filePath1 = rwDirPath + '/' + fileName1;
            String filePath2 = roDirPath + '/' + fileName2;
            
            String script = 
                    "mkdir -p " + roDirPath + "; " +
                    "mkdir -p " + rwDirPath + "; " +
                    "touch " + filePath1 + "; " +
                    "touch " + filePath2 + "; " +
                    "chmod a-r " + roDirPath;
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing sc    ript \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);
            refreshParent(roDirPath);
            RemoteFileObject roDirFO = getFileObject(roDirPath);
            assertFalse("Should not be readable: " + roDirFO, roDirFO.canRead());
            FileObject rwDirFO = getFileObject(rwDirPath);
            FileObject fileFO1 = getFileObject(filePath1);
            DirectoryStorage storage;
            FileObject[] children;
            FileObject invalid;
            
            children = roDirFO.getChildren();
            assertEquals("children size for " + roDirFO.getPath(), 1, children.length);
            
            invalid = roDirFO.getFileObject("inexistent1");
            assertNull("file objject should be null for inexistent1", invalid);
            children = roDirFO.getChildren();
            assertEquals("children size for " + roDirFO.getPath(), 1, children.length);
            storage = ((RemoteDirectory) roDirFO.getImplementor()).testGetExistingDirectoryStorage();            
            assertEquals("storage.size for " + roDirFO.getPath(), 2, storage.listAll().size());
            
            FileObject fileFO2 = getFileObject(filePath2);
            children = roDirFO.getChildren();
            assertEquals("children size for " + roDirFO.getPath(), 2, children.length);
            storage = ((RemoteDirectory) roDirFO.getImplementor()).testGetExistingDirectoryStorage();
            assertEquals("storage.size", 3, storage.listAll().size());
            
            invalid = roDirFO.getFileObject("inexistent2");
            assertNull("file objject should be null for inexistent2", invalid);
            children = roDirFO.getChildren();
            assertEquals("children size for " + roDirFO.getPath(), 2, children.length);
            storage = ((RemoteDirectory) roDirFO.getImplementor()).testGetExistingDirectoryStorage();
            assertEquals("storage.size for " + roDirFO.getPath(), 4, storage.listAll().size());
            
            roDirFO.refresh();
            children = roDirFO.getChildren();
            assertEquals("children size for " + roDirFO.getPath(), 2, children.length);
            storage = ((RemoteDirectory) roDirFO.getImplementor()).testGetExistingDirectoryStorage();
            assertEquals("storage.size for " + roDirFO.getPath(), 2, storage.listAll().size());            
        } finally {
            if (baseDir != null) {
                ProcessUtils.ExitStatus res = ProcessUtils.execute(getTestExecutionEnvironment(), "chmod", "-R", "700", baseDir);
                removeRemoteDirIfNotNull(baseDir);
            }
        }        
    }
    
    public static Test suite() {
        return RemoteApiTest.createSuite(ReadOnlyDirTestCase.class);
    }
}
