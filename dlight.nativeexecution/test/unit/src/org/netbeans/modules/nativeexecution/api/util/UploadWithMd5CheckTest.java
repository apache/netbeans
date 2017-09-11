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

package org.netbeans.modules.nativeexecution.api.util;

import java.io.File;
import java.io.PrintWriter;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport.UploadStatus;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;

/**
 *
 * @author Vladimir Kvashin
 */
public class UploadWithMd5CheckTest extends NativeExecutionBaseTestCase {

    public UploadWithMd5CheckTest(String name, ExecutionEnvironment testExecutionEnvironment) {
        super(name, testExecutionEnvironment);
    }

    @SuppressWarnings("unchecked")
    public static Test suite() {
        return new NativeExecutionBaseTestSuite(UploadWithMd5CheckTest.class);
    }

    @ForAllEnvironments(section = "remote.platforms")
    public void testUploadWithMd5Check() throws Exception {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        ConnectionManager.getInstance().connectTo(env);
        clearRemoteTmpDir();
        String remoteTmpDir = createRemoteTmpDir()  + "/inexistent_subdir";
        File localFile = getIdeUtilJar();
        String remotePath = remoteTmpDir + "/" + localFile.getName();
        int rc = CommonTasksSupport.rmDir(env, remoteTmpDir, true, new PrintWriter(System.err)).get();
        assertEquals("Can not delete directory " + remoteTmpDir, 0, rc);
        assertFalse("File " + env + ":" + remoteTmpDir + " should not exist at this moment", HostInfoUtils.fileExists(env, remotePath));
        assertFalse("File " + env + ":" + remotePath + " should not exist at this moment", HostInfoUtils.fileExists(env, remotePath));
        int uploadCount = SftpSupport.getUploadCount();
        long firstTime = System.currentTimeMillis();
        UploadStatus res = CommonTasksSupport.uploadFile(localFile, env, remotePath, 0777, true).get();
        assertEquals("Error uploading file " + localFile.getAbsolutePath() + " to " + getTestExecutionEnvironment() + ":" + remotePath, 0, rc);
        firstTime = System.currentTimeMillis() - firstTime;
        assertEquals("Error copying " + localFile + " file to " + env + ":" + remotePath + ' ' + res.getError(), 0, res.getExitCode());
        assertTrue("File " + env + ":" + remotePath + " should exist at this moment", HostInfoUtils.fileExists(env, remotePath));
        assertEquals("Uploads count", ++uploadCount, SftpSupport.getUploadCount());
        System.err.printf("First copying %s to %s took %d ms\n", localFile.getAbsolutePath(), remotePath, firstTime);

        for (int pass = 0; pass < 8; pass++) {
            if (pass % 3 == 1) {
                if (pass == 1) {
                    CommonTasksSupport.rmFile(env, remotePath, null).get();
                } else {
                    ProcessUtils.execute(env, "cp", "/bin/ls", remotePath);
                }
                uploadCount++;
            }
            long currTime = System.currentTimeMillis();
            UploadStatus uploadStatus = CommonTasksSupport.uploadFile(localFile, env, remotePath, 0777, true).get();
            firstTime = System.currentTimeMillis() - currTime;
            assertEquals("Error copying " + localFile + " file to " + env + ":" + remotePath + ' ' + uploadStatus.getError(), 0, uploadStatus.getExitCode());
            assertTrue("File " + env + ":" + remotePath + " should exist at this moment", HostInfoUtils.fileExists(env, remotePath));
            assertEquals("Uploads count on pass " + pass, uploadCount, SftpSupport.getUploadCount());
            System.err.printf("Copying (pass %d) %s to %s took %d ms\n", pass, localFile.getAbsolutePath(), remotePath, firstTime);
        }
        clearRemoteTmpDir();
    }
}
