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
public class WritingQueueTestCase extends RemoteFileTestBase {

    public WritingQueueTestCase(String testName) {
        super(testName);
    }
    
    public WritingQueueTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testMultipleWrite() throws Exception {
        String tempFile = null;
        try {
            tempFile = mkTempAndRefreshParent();
            FileObject fo = getFileObject(tempFile);
            
            StringBuilder ref = new StringBuilder();
            for (int i = 0; i < 200000; i++) {
                ref.append(' ');                
            }            

            final int triesCount = 8;
            
            for (int i = 0; i < triesCount; i++) {
                ref.replace(0, ref.length()-1, "" + i);
                writeFile(fo, ref);
            }
            String readContent = ProcessUtils.execute(execEnv, "cat", tempFile).getOutputString();
            if (!readContent.contentEquals(ref)) {
                assertTrue("File content differ: expected " + ref.substring(0, 32) + 
                        "... but was " + readContent.substring(0, 32) + "...", false);
            }
            
        } finally {
            if (tempFile != null) {
                removeRemoteDirIfNotNull(tempFile);
            }
        }
    }
    
    // see #198200 - Deadlock after closing full remote project 
    @ForAllEnvironments
    public void testNonBlockingWrite() throws Exception {
        String tempFile = null;
        try {
            tempFile = mkTempAndRefreshParent();
            FileObject fo = getFileObject(tempFile);
            
            final String data = new String(new byte[200000]);

            int failuresCount = 0;
            final int triesCount = 4;
            final int ratio = 10;
            
            for (int i = 0; i < triesCount; i++) {
                
                long time1 = System.currentTimeMillis();
                writeFile(fo, data);
                time1 = System.currentTimeMillis() - time1;

                long time2 = System.currentTimeMillis();
                writeFile(fo, data);
                time2 = System.currentTimeMillis() - time2;

                System.err.printf("First: %d curr: %d\n", time1, time2);
                if (time2 > time1 * ratio) {
                    failuresCount++;
                }

            }
            
            if (failuresCount == triesCount) {
                assertTrue("2-nd write is bo", false);
            }
            
        } finally {
            if (tempFile != null) {
                removeRemoteDirIfNotNull(tempFile);
            }
        }
    }

    
    public static Test suite() {
        return RemoteApiTest.createSuite(WritingQueueTestCase.class);
    }
}
