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
package org.netbeans.modules.cnd.remote.support;

import org.netbeans.modules.cnd.remote.test.RemoteTestBase;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import junit.framework.Test;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;

/**
 */
public class UploadTestCase extends RemoteTestBase {


    public UploadTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);        
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createRemoteTmpDir();
    }

    @Override
    protected void tearDown() throws Exception {
        clearRemoteTmpDir(); // before disconnection!
        super.tearDown();
    }

    @ForAllEnvironments
    public void testCopyTo() throws Exception {
        File localFile = File.createTempFile("cnd", ".cnd"); //NOI18N
        localFile.deleteOnExit();
        FileWriter fstream = new FileWriter(localFile);
        StringBuilder sb = new StringBuilder("File from "); //NOI18N
        try {
            InetAddress addr = InetAddress.getLocalHost();
            sb.append( addr.getHostName() );
        } catch (UnknownHostException e) {
        }
        sb.append("\ntime: ").append(System.currentTimeMillis());//.append("\n"); //NOI18N
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(sb.toString());
        out.close();
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();        
        String remoteFile = getRemoteTmpDir() + "/" + localFile.getName(); //NOI18N
        int rc = CommonTasksSupport.uploadFile(localFile.getAbsolutePath(), execEnv, remoteFile, 0770).get().getExitCode();
        assertEquals("Upload RC for " + localFile.getAbsolutePath(), 0, rc);
        assert HostInfoProvider.fileExists(execEnv, remoteFile) : "Error copying file " + remoteFile + " to " + execEnv + " : file does not exist";
        ProcessUtils.ExitStatus rcs2 = ProcessUtils.execute(execEnv, "cat", remoteFile);
//            assert rcs2.run() == 0; // add more output
        rc = rcs2.exitCode;
        if (rc != 0) {
            assert false : "RemoteCommandSupport: " + "cat " + remoteFile + " returned " + rc + " on " + execEnv;
        }
        assert rcs2.getOutputString().equals(sb.toString());
        ProcessUtils.ExitStatus rc3 = ProcessUtils.execute(execEnv, "rm", remoteFile);
        assert rc3.exitCode == 0;
        localFile.delete();
    }
    
    public static Test suite() {
        return new RemoteDevelopmentTest(UploadTestCase.class);
    }

    @ForAllEnvironments
    @org.netbeans.api.annotations.common.SuppressWarnings("RV")
    public void testCopyManyFilesTo() throws Exception {
        File dir = new File(getNetBeansPlatformDir(), "modules");
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
        File[] files = dir.listFiles();
        assert files != null;
        long totalSize = 0;
        int totalCount = 0;
        long totalTime = System.currentTimeMillis();
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        ConnectionManager.getInstance().connectTo(execEnv);

        File tmpFile = File.createTempFile("copy_small_files", ".dat");
        String remoteDir = getRemoteTmpDir() + "/" + tmpFile.getName();
        tmpFile.delete();

        Future<Integer> mkDirTask = CommonTasksSupport.mkDir(execEnv, remoteDir, new PrintWriter(System.err));
        System.out.printf("Mkdir %s\n", remoteDir);
        int rc = mkDirTask.get(30, TimeUnit.SECONDS);
        System.out.printf("mkdir %s done, rc=%d\n", remoteDir, rc);
        assertEquals(0, rc);
        //Thread.sleep(2000);

        for (File localFile : files) {
            if (localFile.isFile()) {
                totalCount++;
                totalSize += localFile.length();
                assertTrue(localFile.exists());
                String remoteFile = remoteDir + "/" + localFile.getName(); //NOI18N
                long time = System.currentTimeMillis();
                rc = CommonTasksSupport.uploadFile(localFile.getAbsolutePath(), execEnv, remoteFile, 0770).get().getExitCode();
                assertEquals("Upload RC for " + localFile.getAbsolutePath(), 0, rc);
                time = System.currentTimeMillis() - time;
                System.out.printf("File %s copied to %s:%s in %d ms\n", localFile, execEnv, remoteFile, time);
            }
        }
        totalTime = System.currentTimeMillis() - totalTime;
        System.out.printf("%d Kb in %d files to %s in %d ms\n", totalSize/1024, totalCount, execEnv, totalTime);
        assertEquals("Can't remove " + remoteDir + " on remote host", 0, CommonTasksSupport.rmDir(execEnv, remoteDir, true, new PrintWriter(System.err)).get().intValue());
    }

}
