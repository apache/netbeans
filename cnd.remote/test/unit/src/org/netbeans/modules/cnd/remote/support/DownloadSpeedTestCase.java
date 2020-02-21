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

package org.netbeans.modules.cnd.remote.support;

import org.netbeans.modules.cnd.remote.test.RemoteTestBase;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import junit.framework.Test;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;

/**
 * Pseudo-test for download speed measurements
 */
public class DownloadSpeedTestCase extends RemoteTestBase {

    public DownloadSpeedTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testBatchDownload() throws Exception {
        File listFile = new File("/tmp/download.list");
        assertTrue(listFile.exists());
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(listFile)));
        List<String> remoteFiles = new ArrayList<>(1000);
        String line;
        while ((line = br.readLine()) != null) {
            if (line.length() > 0) { // ignore empty lines (usually trailing)
                remoteFiles.add(line);
            }
        }
        ExecutionEnvironment env = getTestExecutionEnvironment();
        PrintWriter err = new PrintWriter(System.err);
        File destDir = getWorkDir(); //  + "/download-speed-test"
        destDir.mkdirs();
        assertTrue(destDir.exists());
        long time = System.currentTimeMillis();
        int cnt = 0;
        for (String remotePath : remoteFiles) {
            String name = CndPathUtilities.getBaseName(remotePath);
            File localFile = new File(destDir, name + '.' + (cnt++));
            Future<Integer> task = CommonTasksSupport.downloadFile(remotePath, env, localFile, err);
            int rc = task.get().intValue();
            assertEquals(0, rc);
        }
        time = System.currentTimeMillis() - time;
        System.err.printf("Downloaded %d files from %s in %d seconds\n", remoteFiles.size(), env, time/1000);
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(DownloadSpeedTestCase.class);
    }

}
