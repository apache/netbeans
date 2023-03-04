/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
