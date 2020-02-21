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
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class CreateDataAndFolderTestCase extends RemoteFileTestBase {

    public CreateDataAndFolderTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testCreateFolder() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);
            FileObject baseDirFO = getFileObject(baseDir);
            
            String childName;
            String childAbsPath;
            FileObject childFO;
            Exception exception;
                    
            childName = "child_folder";
            
            childFO = baseDirFO.createFolder(childName);
            assertNotNull("Null child file object", childFO.isValid());
            assertTrue("child file object isn't valid", childFO.isValid());
            assertTrue("child isn't a directory", childFO.isFolder());
            childAbsPath = baseDir + '/' + childName;
            assertTrue("Child file should exist: " + childAbsPath, HostInfoUtils.fileExists(execEnv, childAbsPath));
            
            exception = null;
            try {
                baseDirFO.createFolder(childName);
            } catch (IOException e) {  
                exception = e;
            }
            assertTrue("Creating the same directory twice; an IOException should be thrown", exception != null);
            
            childName = "child folder with a space";
            childFO = baseDirFO.createFolder(childName);
            assertNotNull("Null child file object", childFO.isValid());
            assertTrue("child file object isn't valid", childFO.isValid());
            assertTrue("child isn't a directory", childFO.isFolder());
            childAbsPath = baseDir + '/' + childName;
            assertTrue("Child file should exist: " + childAbsPath, HostInfoUtils.fileExists(execEnv, childAbsPath));
            
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }
    
    

    @ForAllEnvironments
    public void testCreateData() throws Exception {
        String baseDir = mkTempAndRefreshParent(true);
        try {
            baseDir = mkTempAndRefreshParent(true);
            FileObject baseDirFO = getFileObject(baseDir);
            
            String childName;
            String childAbsPath;
            FileObject childFO;
            Exception exception;
                       
            childName = "child_file";
            childFO = baseDirFO.createData(childName);
            assertNotNull("Null child file object", childFO.isValid());
            assertTrue("child file object isn't valid", childFO.isValid());
            assertTrue("child isn't a plain file", childFO.isData());
            childAbsPath = baseDir + '/' + childName;
            assertTrue("Child file should exist: " + childAbsPath, HostInfoUtils.fileExists(execEnv, childAbsPath));
            
            exception = null;
            try {
                childFO = baseDirFO.createData(childName);
            } catch(IOException e) {
                exception = e;
            }
            assertTrue("Creating the same file twice; an IOException should be thrown", exception != null);
            
            childName = "child file with a space";
            childFO = baseDirFO.createData(childName);
            assertNotNull("Null child file object", childFO.isValid());
            assertTrue("child file object isn't valid", childFO.isValid());
            assertTrue("child isn't a plain file", childFO.isData());
            childAbsPath = baseDir + '/' + childName;
            assertTrue("Child file should exist: " + childAbsPath, HostInfoUtils.fileExists(execEnv, childAbsPath));
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }
    
    public static Test suite() {
        return RemoteApiTest.createSuite(CreateDataAndFolderTestCase.class);
    }
}
