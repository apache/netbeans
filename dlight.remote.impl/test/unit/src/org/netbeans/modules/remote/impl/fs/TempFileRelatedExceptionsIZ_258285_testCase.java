/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.remote.impl.fs;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import junit.framework.Test;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class TempFileRelatedExceptionsIZ_258285_testCase extends RemoteFileTestBase {

    public TempFileRelatedExceptionsIZ_258285_testCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testDirectoryLink() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);
            final FileObject baseDirFO = getFileObject(baseDir);

            final int childCnt = 10;
            final FileObject[] children = new FileObject[childCnt];
            for (int i = 0; i < childCnt; i++) {
                children[i] = baseDirFO.createData("child_" + i);
            }

            final CyclicBarrier barrier = new CyclicBarrier(2);

            final int writeCnt = 100;
            final List<Exception> writeExceptions = Collections.synchronizedList(new ArrayList<Exception>());
            Thread writer = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException ex ) {
                        ex.printStackTrace();
                    }
                    for (int i = 0; i < writeCnt; i++) {
                        //System.out.println("Writing, pass # " + i);
                        for (int j = 0; j < childCnt; j++) {
                            try {
                                writeFile(children[j], "" + System.currentTimeMillis());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                writeExceptions.add(ex);
                            }
                        }
                    }
                }
            });

            final int readCnt = 100;
            final AtomicInteger errorCount = new AtomicInteger(0);
            Thread reader = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException ex ) {
                        ex.printStackTrace();
                    }
                    for (int i = 0; i < readCnt; i++) {
                        //System.out.println("Reading, pass # " + i);
                        baseDirFO.refresh();
                        FileObject[] freshChildren = baseDirFO.getChildren();
                        for (FileObject child : freshChildren) {
                            String name = child.getNameExt();
                            if (name.startsWith("#")) {
                                errorCount.incrementAndGet();
                                //System.out.println("Unexpected " + name);
                            }
                        }
                    }
                }
            });
            writer.start();
            reader.start();
            writer.join();
            reader.join();
            assertEquals("Conflicts count", 0, errorCount.get());
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(TempFileRelatedExceptionsIZ_258285_testCase.class);
    }
}
