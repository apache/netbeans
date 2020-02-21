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
