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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.test.RemoteApiTest;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class RemoteLinksChangeLinkTestCase2 extends RemoteFileTestBase {

    public RemoteLinksChangeLinkTestCase2(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        RemoteFileObjectFactory.testSetReportUnexpected(false);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        RemoteFileObjectFactory.testSetReportUnexpected(true);
    }

    @ForAllEnvironments
    public void testClassCast_iz209461() throws Exception {
        final String baseDir = mkTempAndRefreshParent(true);
        try {
            final String realDir =        "real_dir";
            final String realFile =       "real_dir/file";
            final String dirOrLink =      "dir_or_link";
            final String dirOrLinkChild =  "dir_or_link/file";

            final String changeToLinkScript =
                    "cd " + baseDir + "; " +
                    "mkdir -p " + realDir + "; " +
                    "touch " + realFile + "; " +
                    "rm -rf " + dirOrLink + "; " +
                    "ln -s " + realDir + ' ' + dirOrLink + "; " +
                    "";

            final String changeToRealFileScript =
                    "cd " + baseDir + "; " +
                    "mkdir -p " + realDir + "; " +
                    "touch " + realFile + "; " +
                    "rm -rf " + dirOrLink + "; " +
                    "mkdir -p " + dirOrLink + "; " +
                    "touch " + dirOrLinkChild + "; " +
                    "";

            final String dirOrLinkAbsPath = baseDir + '/' + dirOrLink;
            final String dirOrLinkChildAbsPath = baseDir + '/' + dirOrLinkChild;

            ProcessUtils.ExitStatus res0 = ProcessUtils.execute(execEnv, "sh", "-c", changeToLinkScript);
            assertEquals("Error executing script \"" + changeToLinkScript + "\": " + res0.getErrorString(), 0, res0.exitCode);

            final RemoteFileObject baseDirFO = getFileObject(baseDir);

            final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<Throwable>());

            final AtomicBoolean stop = new AtomicBoolean(false);
            final AtomicInteger lastId = new AtomicInteger(0);

            final AtomicInteger plainFileCount = new AtomicInteger(0);
            final AtomicInteger childLinkCount = new AtomicInteger(0);
            final AtomicInteger nullCount = new AtomicInteger(0);
            final AtomicInteger changeCount = new AtomicInteger(0);

            Thread changer = new Thread(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setName("Changer");
                    try {
                        int pass = 0;
                        do {
                            //System.out.printf("Changer pass #%d\n", ++pass);
                            //System.out.printf("Changing %s to real directory...\n", dirOrLinkAbsPath);
                            ProcessUtils.ExitStatus res2 = ProcessUtils.execute(execEnv, "sh", "-c", changeToRealFileScript);
                            if (!res2.isOK()) {
                                System.err.println("Error executing script \"" + changeToRealFileScript + "\": " + res2.getErrorString());
                            }
                            //System.out.printf("Refreshing...\n");
                            baseDirFO.refresh();
                            //System.out.printf("Refreshed. Changing %s to link\n", dirOrLinkAbsPath);
                            ProcessUtils.ExitStatus res1 = ProcessUtils.execute(execEnv, "sh", "-c", changeToLinkScript);
                            if (!res1.isOK()) {
                                System.err.println("Error executing script \"" + changeToLinkScript + "\": " + res1.getErrorString());
                            }
                            //System.out.printf("Refreshing...\n");
                            baseDirFO.refresh();
                            changeCount.incrementAndGet();
                        } while (!stop.get());
                    } catch (Throwable ex) {
                        exceptions.add(ex);
                        ex.printStackTrace(System.err);
                        stop.set(true);
                    }
                }
            });

            class Reader implements Runnable {
                
                private final int id;

                public Reader() {
                    id = lastId.incrementAndGet();
                }

                @Override
                public void run() {
                    Thread.currentThread().setName("Reader #" + id);
                    try {
                        int pass = 0;
                        do {
                            //System.out.printf("Reader #%d pass #%d\n", id, ++pass);
                            RemoteFileObject dirOrLinkChildFO1 = rootFO.getFileObject(dirOrLinkChildAbsPath);
                            RemoteFileObjectBase dirOrLinkChildFOimpl1 = (dirOrLinkChildFO1 == null) ? null : dirOrLinkChildFO1.getImplementor();
                            if (dirOrLinkChildFOimpl1 instanceof RemoteLinkChild) {
                                childLinkCount.incrementAndGet();
                            } else if (dirOrLinkChildFOimpl1 instanceof RemotePlainFile) {
                                plainFileCount.incrementAndGet();
                            } else if (dirOrLinkChildFOimpl1 == null) {
                                nullCount.incrementAndGet();
                            } else {
                                throw new Exception("Wrong instance: " + dirOrLinkChildFOimpl1.getClass().getName());
                            }
                        } while (!stop.get());
                    } catch (Throwable ex) {
                        exceptions.add(ex);
                        ex.printStackTrace(System.err);
                        stop.set(true);
                    }
                }
            };
            Thread accessor1 = new Thread(new Reader());
            Thread accessor2 = new Thread(new Reader());
            changer.start();
            accessor1.start();
            accessor2.start();

            for (int i = 0; i < 30 && !stop.get(); i++) {
                sleep(1000);
            }

            stop.set(true);

            accessor1.join(2000);
            accessor2.join(2000);
            changer.join(20000);

            if (Boolean.getBoolean("RemoteLinksChangeLinkTestCase2.trace")) {
                System.out.printf("testClassCast_iz209461 changes count:   %d\n", changeCount.get());
                System.out.printf("testClassCast_iz209461 plain count:     %d\n", plainFileCount.get());
                System.out.printf("testClassCast_iz209461 links count:     %d\n", childLinkCount.get());
                System.out.printf("testClassCast_iz209461 null count:      %d\n", nullCount.get());
            }

            if (!exceptions.isEmpty()) {
                System.err.printf("There were %d exceptions; throwing first one.\n", exceptions.size());
                exceptions.iterator().next().printStackTrace(System.err);
                assertTrue("Exceptions", exceptions.isEmpty());
            }
            
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(RemoteLinksChangeLinkTestCase2.class);
    }
}
