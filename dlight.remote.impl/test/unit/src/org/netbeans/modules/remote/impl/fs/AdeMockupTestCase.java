/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.impl.fs;

import junit.framework.Test;
import org.netbeans.modules.remote.test.RemoteApiTest;
import java.io.IOException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class AdeMockupTestCase  extends RemoteFileTestBase  {
    
    public AdeMockupTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testInstances() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);

            String fileName1 = "file1";
            String fileName2 = "file2";

            String script1 = // file1, file2 - plain files
                    "cd " + baseDir + "; " +
                    "rm -rf *; " +
                    "touch "+ fileName1 + "; " +                    
                    "touch "+ fileName2 + "; " ;

            String script2 = // file1 - plain file, file2 - link
                    "cd " + baseDir + "; " +
                    "rm -rf *; " +
                    "touch "+ fileName1 + "; " +                    
                    "ln -s " + fileName1 + ' ' + fileName2 + ";";

            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script1);
            assertEquals("Error executing script \"" + script1 + "\": " + res.getErrorString(), 0, res.exitCode);
            
            FileObject baseDirFO = getFileObject(baseDir);
            
            baseDirFO.refresh();           
            FileObject fo1_1 = baseDirFO.getFileObject(fileName1);
            FileObject fo2_1 = baseDirFO.getFileObject(fileName2);
            assertNotNull(fo1_1);
            assertNotNull(fo2_1);
            
            res = ProcessUtils.execute(execEnv, "sh", "-c", script2);
            assertEquals("Error executing script \"" + script1 + "\": " + res.getErrorString(), 0, res.exitCode);
                        
            baseDirFO.refresh();
            
            FileObject fo1_2 = baseDirFO.getFileObject(fileName1);
            FileObject fo2_2 = baseDirFO.getFileObject(fileName2);
            assertNotNull(fo1_2);
            assertNotNull(fo2_2);
            assertTrue("Instances differ for " + fo1_1.getPath(), fo1_1 == fo1_2);
            assertTrue("Instances differ for " + fo2_1.getPath(), fo2_1 == fo2_2);
            
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }
    
    public static Test suite() {
        return RemoteApiTest.createSuite(AdeMockupTestCase.class);
    }
    
}
