/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * That's not for including in daily tests,
 * but rather to reproducing the situation when a file can not be open because it is locked
 *
 */
public class ReadWriteLocksTestCase extends RemoteFileTestBase {

//    static {
//        //System.setProperty("remote.fs_server", "false");
//        //System.setProperty("remote.fs_server.verbose", "3");
//        System.setProperty("remote.fs_server.suppress.stderr", "false");
//        //System.setProperty("remote.fs_server.refresh", "60000"); // NOI18N
//    }    
        
    public ReadWriteLocksTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    private void readFile(FileObject fo, boolean close) {
        BufferedReader reader = null;
        try {
            InputStream is = fo.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            char buf[] = new char[4096];
            int cnt = 0;
            while ((cnt = reader.read(buf)) != -1) {
                String text = String.valueOf(buf, 0, cnt);
                sb.append(text);
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (close && reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void writeFile(FileObject fo, String text) {
        OutputStream os = null;
        try {
            FileLock lock = fo.lock();
            os = fo.getOutputStream(lock);
            os.write(text.getBytes());            
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @ForAllEnvironments
    public void testReadWriteLocks() throws Exception {
        System.err.println("\n========== local ==========\n");
        doTestLocal();
        System.err.println("\n========== remote ==========\n");
        doTestRemote();
    }

    private void doTestLocal() throws Exception {
        File dataDir = getDataDir();
        dataDir.mkdirs();
        FileUtil.refreshFor(dataDir.getParentFile());
        FileObject dataFO = FileUtil.toFileObject(dataDir);
        FileObject subDirFO = dataFO.createFolder("testReadWriteLocks");
        doTest(subDirFO);
    }

    private void doTestRemote() throws Exception {
        String remoteBaseDir = null;
        try {
            remoteBaseDir = mkTempAndRefreshParent(true);
            FileObject subDirFO = getFileObject(remoteBaseDir).createFolder("testReadWriteLocks");
            doTest(subDirFO);
        } finally {
            removeRemoteDirIfNotNull(remoteBaseDir);
        }
    }

    private abstract class WaitingRunnable extends Thread {

        public final CountDownLatch waitBefore = new CountDownLatch(1);
        public final CountDownLatch waitAfter = new CountDownLatch(1);

        public WaitingRunnable(String name) {
            super(name);
        }

        @Override
        public final void run() {
            try {
                waitBefore.await();
                runImpl();
                waitAfter.await();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        protected abstract void runImpl();
    }

    private void doTest(FileObject subDirFO) throws InterruptedException, IOException {
        final FileObject fo = subDirFO.createData("text.txt");
        sleep(1000);        
        WaitingRunnable closingReader = new WaitingRunnable("closingReader") {
            @Override
            public void runImpl() {
                readFile(fo, true);
            }
        };
        WaitingRunnable nonClosingReader1 = new WaitingRunnable("nonClosingReader #1") {
            @Override
            public void runImpl() {
                readFile(fo, false);
            }
        };
        WaitingRunnable nonClosingReader2 = new WaitingRunnable("nonClosingReader #2") {
            @Override
            public void runImpl() {
                readFile(fo, false);
            }
        };
        WaitingRunnable writer = new WaitingRunnable("writer") {
            @Override
            public void runImpl() {
                writeFile(fo, "Nee file content\n");
            }
        };
        WaitingRunnable[] all = new WaitingRunnable[] {closingReader, nonClosingReader1, nonClosingReader2, writer};
        for (WaitingRunnable r : all) {
            r.start();
        }
        nonClosingReader1.waitBefore.countDown();
        nonClosingReader2.waitBefore.countDown();
        sleep(100);
        closingReader.waitBefore.countDown();
        sleep(100);
        writer.waitBefore.countDown();
        sleep(100);
        sleep(10*1000);
        for (WaitingRunnable r : all) {
            r.waitAfter.countDown();
            r.join();
        }
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(ReadWriteLocksTestCase.class);
    }
}
