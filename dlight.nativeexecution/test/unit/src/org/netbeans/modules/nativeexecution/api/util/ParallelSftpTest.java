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
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Future;
import junit.framework.Test;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport.UploadStatus;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Kvashin
 */
public class ParallelSftpTest extends NativeExecutionBaseTestCase {
    
    private final Writer errorWriter = new PrintWriter(System.err);
    
    public ParallelSftpTest(String name, ExecutionEnvironment testExecutionEnvironment) {
        super(name, testExecutionEnvironment);
    }

    @ForAllEnvironments(section = "remote.platforms")
    public void testMultipleDownload() throws Exception {
        int taskCount = 200;
        int concurrencyLevel = 10;
        SftpSupport.testSetConcurrencyLevel(concurrencyLevel);        
        ExecutionEnvironment env = getTestExecutionEnvironment();
        ConnectionManager.getInstance().connectTo(env);
        File localTmpDir = createTempFile("parallel", "upload", true);
        long time = System.currentTimeMillis();
        try {            
            @SuppressWarnings("unchecked")
            Future<Integer>[] tasks = (Future<Integer>[]) (new Future[taskCount]);
            File[] files = new File[taskCount];
            for (int i = 0; i < taskCount; i++) {
                files[i] = new File(localTmpDir, "dst_" + i);
                tasks[i] = CommonTasksSupport.downloadFile("/usr/include/stdio.h", env, files[i], errorWriter);
            }
            for (int i = 0; i < taskCount; i++) {
                assertEquals("RC for task #" + i, 0, tasks[i].get().intValue());
            }
        } finally {
            time = System.currentTimeMillis() - time;
            removeDirectory(localTmpDir);
        }        
        System.err.printf("%d downloads took %d seconds; declared concurrency level: %d; max. SFTP busy channels: %d\n", 
                taskCount, time/1000, concurrencyLevel, SftpSupport.getInstance(env).getMaxBusyChannels());
        //System.err.printf("Max. SFTP busy channels: %d\n", SftpSupport.getInstance(env).getMaxBusyChannels());
    }

    /** returns ONLY FILES, no directories */
    private static StatInfo[] ls(ExecutionEnvironment env, String remoteDir) throws Exception {
        Future<StatInfo[]> lsTask = FileInfoProvider.ls(env, remoteDir);
        StatInfo[] ls = lsTask.get();
        assertTrue("too few elements in ls /usr/include RC", ls.length > 10);        
        List<StatInfo> result = new ArrayList<>(ls.length);
        for (int i = 0; i < ls.length; i++) {
            if(!ls[i].isDirectory() && ! ls[i].isLink()) {
                result.add(ls[i]);
            }            
        }
        return result.toArray(new StatInfo[result.size()]);
    }
    
    @ForAllEnvironments(section = "remote.platforms")
    public void testParallelMultyDownload() throws Exception {
        final int threadCount = 10;
        final int concurrencyLevel = 10;
        SftpSupport.testSetConcurrencyLevel(concurrencyLevel);
        final ExecutionEnvironment env = getTestExecutionEnvironment();
        ConnectionManager.getInstance().connectTo(env);
        final File localTmpDir = createTempFile("parallel", "upload", true);
        final String remoteDir = "/usr/include";
        final StatInfo[] ls = ls(env, remoteDir);
        assertTrue("too few elements in ls /usr/include RC", ls.length > 10);        
        long time = System.currentTimeMillis();
        try {            
            @SuppressWarnings("unchecked")
            final Future<Integer>[][] tasks = (Future<Integer>[][]) (new Future[threadCount][ls.length]);
            final File[][] files = new File[threadCount][ls.length];            
            final CyclicBarrier barrier = new CyclicBarrier(threadCount);
            Thread[] threads = new Thread[threadCount];
            final Exception[] threadExceptions = new Exception[threadCount];
            for (int i = 0; i < threadCount; i++) {
                final int currThread = i;
                final String threadName = "SFTP parallel download test thread #" + currThread;
                threads[currThread] = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.err.printf("%s waiting on barrier\n", threadName);
                            barrier.await();
                            System.err.printf("%s started\n", threadName);
                            try {
                                for (int currFile = 0; currFile < ls.length; currFile++ ) {
                                    String name = ls[currFile].getName();
                                    files[currThread][currFile] = new File(localTmpDir, name + '.' + currThread);
                                    tasks[currThread][currFile] = CommonTasksSupport.downloadFile(remoteDir + '/' + name, env, files[currThread][currFile], errorWriter);
                                }
                            } finally {
                                System.err.printf("%s finished\n", threadName);
                            }
                        } catch (InterruptedException ex) {
                            threadExceptions[currThread] = ex;
                            Exceptions.printStackTrace(ex);
                        } catch (BrokenBarrierException ex) {
                            threadExceptions[currThread] = ex;
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }, threadName);
            }
            
            for (int i = 0; i < threadCount; i++) {
                threads[i].start();
            }
            System.err.printf("Waiting for threads to finish\n");
            for (int i = 0; i < threadCount; i++) {
                threads[i].join();
            }
            
            for (int i = 0; i < threadCount; i++) {
                if (threadExceptions[i] != null) {
                    throw threadExceptions[i];
                }
            }
                                    
            for (int currLap = 0; currLap < threadCount; currLap++) {
                for (int currFile = 0; currFile < ls.length; currFile++ ) {
                    assertEquals("RC for file " + files[currLap][currFile].getName() + " lap #" + currLap, 0, tasks[currLap][currFile].get().intValue());
                }
            }
            
        } finally {
            time = System.currentTimeMillis() - time;
            removeDirectory(localTmpDir);
        }        
        System.err.printf("Downloading from %s; %d threads %d files each took %d seconds; declared concurrency level: %d; max. SFTP busy channels: %d\n", 
                remoteDir, threadCount, ls.length, time/1000, concurrencyLevel, SftpSupport.getInstance(env).getMaxBusyChannels());
        //System.err.printf("Max. SFTP busy channels: %d\n", SftpSupport.getInstance(env).getMaxBusyChannels());
    }

    private void gatherPlainFiles(File dir, Collection<File> result, Set<String> shotrNames) throws Exception {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                gatherPlainFiles(file, result, shotrNames);
            } else {
                if (!shotrNames.contains(file.getName())) {
                    result.add(file);
                }
            }
        }
    }
    
    private File[] getNetBeansPlatformPlainFiles() throws Exception {
        List<File> result = new ArrayList<>();
        File platformDir = getNetBeansPlatformDir();
        assertNotNull("netbeans platform dir", platformDir);
        gatherPlainFiles(platformDir, result, new TreeSet<String>());
        return result.toArray(new File[result.size()]);
    }
    
    @RandomlyFails
    @ForAllEnvironments(section = "remote.platforms")
    public void testParallelUpload() throws Exception {        
        final int threadCount = 10;
        int concurrencyLevel = 10;
        SftpSupport.testSetConcurrencyLevel(concurrencyLevel);        
        final ExecutionEnvironment env = getTestExecutionEnvironment();
        ConnectionManager.getInstance().connectTo(env);
        final File[] files = getNetBeansPlatformPlainFiles();
        final String remoteDir = createRemoteTmpDir();
        long time = System.currentTimeMillis();        
        try {            
            @SuppressWarnings("unchecked")
            final Future<UploadStatus>[][] tasks = (Future<UploadStatus>[][]) (new Future[threadCount][files.length]);
            Thread[] threads = new Thread[threadCount];
            final Exception[] threadExceptions = new Exception[threadCount];
            final CyclicBarrier barrier = new CyclicBarrier(threadCount);            
            for (int i = 0; i < threadCount; i++) {
                final int currThread = i;
                final String threadName = "SFTP parallel upload test thread #" + currThread;
                threads[currThread] = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.err.printf("%s waiting on barrier\n", threadName);
                            barrier.await();
                            System.err.printf("%s started\n", threadName);
                            try {
                                for (int currFile = 0; currFile < files.length; currFile++ ) {
                                    String name = files[currFile].getName();                    
                                    tasks[currThread][currFile] = CommonTasksSupport.uploadFile(
                                            files[currFile], env, remoteDir + '/' + name /*+ '.' + currLap*/, 0600);
                                }
                            } finally {
                                System.err.printf("%s finished\n", threadName);
                            }
                        } catch (InterruptedException ex) {
                            threadExceptions[currThread] = ex;
                            Exceptions.printStackTrace(ex);
                        } catch (BrokenBarrierException ex) {
                            threadExceptions[currThread] = ex;
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }, threadName);
            }
            
            for (int i = 0; i < threadCount; i++) {
                threads[i].start();
            }
            System.err.printf("Waiting for threads to finish\n");
            for (int i = 0; i < threadCount; i++) {
                threads[i].join();
            }
            
            for (int i = 0; i < threadCount; i++) {
                if (threadExceptions[i] != null) {
                    throw threadExceptions[i];
                }
            }
                        
            for (int currLap = 0; currLap < threadCount; currLap++) {
                for (int currFile = 0; currFile < files.length; currFile++ ) {
                    UploadStatus res = tasks[currLap][currFile].get();
                    assertEquals("RC for file " + files[currFile].getName() + " lap #" + currLap + " error:" + res.getError(), 
                            0, res.getExitCode());
                }
            }
        } finally {
            time = System.currentTimeMillis() - time;
            runScript("rm -rf " + remoteDir);
            //runCommand("rm", "rf", remoteDir);
        }        
        System.err.printf("Uploading to %s; %d threads %d files each took %d seconds; declared concurrency level: %d; max. SFTP busy channels: %d\n", 
                remoteDir, threadCount, files.length, time/1000, concurrencyLevel, SftpSupport.getInstance(env).getMaxBusyChannels());
    }
    
    @SuppressWarnings("unchecked")
    public static Test suite() {
        return new NativeExecutionBaseTestSuite(ParallelSftpTest.class);
    }    
}
