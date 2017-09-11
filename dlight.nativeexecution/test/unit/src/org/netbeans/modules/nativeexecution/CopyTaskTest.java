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
package org.netbeans.modules.nativeexecution;

import javax.swing.event.ChangeEvent;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.event.ChangeListener;
import junit.framework.Test;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport.UploadStatus;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;

/**
 *
 * @author ak119685
 */
public class CopyTaskTest extends NativeExecutionBaseTestCase {

    public CopyTaskTest(String name) {
        super(name);
    }

    public CopyTaskTest(String name, ExecutionEnvironment execEnv) {
        super(name, execEnv);
    }

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(CopyTaskTest.class);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @org.junit.Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testCopyToRemote() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        assertNotNull(execEnv);
        File src = createTempFile("test-upload-1", null, false); // NOI18N
        src.deleteOnExit();
        writeFile(src, "qwe/nasd/nzxc"); // NOI18N
        String dst = "/tmp/" + /* execEnv.getUser() + "/" +  */ src.getName(); // NOI18N
        System.err.printf("testUploadFile: %s to %s:%s\n", src.getAbsolutePath(), execEnv.getDisplayName(), dst); // NOI18N

        Future<UploadStatus> uploadTask;
        int rc;

        uploadTask = CommonTasksSupport.uploadFile(src.getAbsolutePath(), execEnv, dst, 0755);
        UploadStatus uploadStatus = uploadTask.get();
        assertEquals("Error uploading " + src.getAbsolutePath() + " to " + execEnv + ":" + dst + ' ' + uploadStatus.getError(), 0, uploadStatus.getExitCode());
        assertTrue(HostInfoUtils.fileExists(execEnv, dst));

        StatInfo statFomrUpload = uploadStatus.getStatInfo();
        StatInfo stat = FileInfoProvider.lstat(execEnv, dst).get();
        assertEquals("Stat got from upload differ", stat.toExternalForm(), statFomrUpload.toExternalForm());

        final AtomicReference<Object> ref = new AtomicReference<>();
        CommonTasksSupport.UploadParameters up = new CommonTasksSupport.UploadParameters(
                src, execEnv, dst, null, 0755, false, new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                ref.set(e.getSource());
            }
        });
        uploadTask = CommonTasksSupport.uploadFile(up);

        rc = uploadTask.get().getExitCode();

        // sleep a bit since listener can be just not calleds
        if (ref.get() == null) {
            sleep(100);
        }
        if (ref.get() == null) {
            sleep(500);
        }

        assertEquals("Error uploading " + src.getAbsolutePath() + " to " + execEnv + ":" + dst, 0, rc);

        assertNotNull("callback wasn't called", ref.get());
        assertEquals("callback was called with incorrect source object", uploadTask, ref.get());

        Future<Integer> res = CommonTasksSupport.rmFile(execEnv, dst, null);
        assertEquals("Error removing " + execEnv + ":" + dst, 0, res.get().intValue());
    }
}
