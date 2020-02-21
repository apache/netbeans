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

package org.netbeans.modules.cnd.remote.sync;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.remote.test.RemoteTestBase;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test for ScpSyncWorker
 */
public class MeasurementTestCase extends RemoteTestBase {

    static {
        System.setProperty("cnd.remote.sync.zip", "true");
    }

    private final File srcDir;
    private final String displayNameBase;
    private static final boolean DEBUG = false;

    private static final List<StatEntry> statistics = new ArrayList<>();

    private static final class StatEntry {
        public String label;
        public long time;
        public StatEntry(String label, long time) {
            this.label = label;
            this.time = time;
        }

    }

    @org.netbeans.api.annotations.common.SuppressWarnings("LG")
    private MeasurementTestCase(String testName, ExecutionEnvironment execEnv, File testDirectory, String displayNameBase) {
        super(testName, execEnv);
        this.srcDir = testDirectory;
        this.displayNameBase = displayNameBase;
    }

   
    private void do_test(boolean removeFileStampsStorage) throws Exception {
        try {
            ExecutionEnvironment execEnv = getTestExecutionEnvironment();
            String dst = getDestDir(execEnv);
            PrintWriter out = new PrintWriter(System.out);
            PrintWriter err = new PrintWriter(System.err);
            System.out.printf("testUploadFile: %s to %s:%s\n", srcDir.getAbsolutePath(), execEnv.getDisplayName(), dst);

            FileObject tmp = FileUtil.toFileObject(FileUtil.normalizeFile(new File(System.getProperty("java.io.tmpdir"))));
            tmp = FileUtil.createFolder(tmp, System.getProperty("user.name"));
            tmp = FileUtil.createFolder(tmp, "testdata");
            FileObject privProjectStorageDir = FileUtil.createFolder(tmp, srcDir.getName() + "-nbproject-private");
            //removeDirectoryContent(privProjectStorageDir);

            FtpSyncWorker worker = new FtpSyncWorker(execEnv, out, err, privProjectStorageDir,
                    Collections.singletonList(FSPath.toFSPath(FileUtil.toFileObject(FileUtil.normalizeFile(srcDir)))),
                    Collections.<FSPath>emptyList());
            long time = System.currentTimeMillis();

            worker.startup(Collections.<String, String>emptyMap());
            worker.shutdown();
            time = System.currentTimeMillis() - time;
            statistics.add(new StatEntry(getName(), time));
            CommonTasksSupport.rmDir(execEnv, dst, true, err).get();
        } catch (Exception e) {
            statistics.add(new StatEntry(getName(), -1));
            throw e;
        } finally {
            if (removeFileStampsStorage) {
                //removeDirectory(privProjectStorageDir);
            }
        }
    }

    public void test_upload_first() throws Exception {
        do_test(true);
    }

    public void test_upload_changed() throws Exception {
        change();
        do_test(false);
    }

    private static final int MAX_SIZE = 1024 * 1024; // max size of files to be changed
    private static final int latch = 10; // we change each X-th file that is less or equal than MAX_SIZE bytes
    private int totalCount;
    private int changedCount;

    private void change() throws IOException {
        totalCount = 0;
        changedCount = 0;
        change(srcDir);
        if (DEBUG) {
            System.out.printf("%d of %d diles changed\n", changedCount, totalCount);
        }
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("OBL")
    private void change(File file) throws IOException {
        if (file.isDirectory()) {
            File[] ff = file.listFiles();
            if (ff != null) {
                for (File child : ff) {
                    change(child);
                }
            }
        } else {
            if (file.length() <= MAX_SIZE) {
                if ((totalCount++) % latch == 0) {
                    changedCount++;
                    //if (DEBUG) { System.out.printf("Changing %s\n", file); }
                    char[] buffer = new char[(int) file.length()];
                    Reader reader = new FileReader(file);
                    int size = reader.read(buffer);
                    reader.close();
                    for (int i = 0; i < buffer.length; i++) {
                        if (Character.isLowerCase(buffer[i])) {
                            buffer[i] = Character.toUpperCase(buffer[i]);
                        } else if(Character.isUpperCase(buffer[i])) {
                            buffer[i] = Character.toLowerCase(buffer[i]);
                        }
                    }
                    Writer writer = new FileWriter(file);
                    writer.write(buffer, 0, size);
                    writer.close();
                }
            }
        }
    }

    private String getDestDir(ExecutionEnvironment execEnv) {
        return getRemoteTmpDir() + "/sync-worker-measurements-test/" + Math.random() + "/";
    }

    @Override
    public String getName() {
        String name = srcDir.getName() + ' ' + displayNameBase;
        ExecutionEnvironment env = getTestExecutionEnvironment();
        if (env == null) {
            return name;
        } else {
            return String.format("%s [%s]", name, env);
        }
    }

    // @BeforeClass
    public static void initializeStatistics() {
        statistics.clear(); // just in case
    }

    // @AfterClass
    public static void printStatistics() {
        PrintStream ps = System.out;
        int max = 0;
        for (StatEntry entry : statistics) {
            max = Math.max(max, entry.label.length());
        }
        //String pattern = String.format("%%%ds %%02d:%%02d:%%02d\n", max+2);
        String okPattern = String.format("%%%ds %%4d:%%02d\n", max+2);
        String erPattern = String.format("%%%ds FAILED\n", max+2);
        ps.printf("================================================================\n");
        for (StatEntry entry : statistics) {
            long time  = entry.time;
            if (time >= 0) {
                long s = (((time%1000) >= 500) ? 1 : 0) + time/1000;
                //long h = s / 3600;
                //long m = (s % 3600) / 60;
                long m = s / 60;
                s = s % 60;
                ps.printf(okPattern, entry.label, m, s);
            } else {
                ps.printf(erPattern, entry.label);
            }
        }
        ps.printf("----------------------------------------------------------------\n");
        statistics.clear(); //
    }

    private static class MeasurementTestSuite extends RemoteDevelopmentTest {

        public MeasurementTestSuite(String name, Collection<Test> tests) {
            super(name, tests);
        }

        @Override
        public void run(TestResult result) {
            initializeStatistics();
            super.run(result);
            printStatistics();
        }       
    }

    public static Test suite() throws IOException, FormatException {
        Collection<Test> tests = new ArrayList<>();
        RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        Collection<String> dirs = rcFile.getKeys("remote.zip.testdirs");
        Collection<String> mspecs = rcFile.getKeys(RemoteDevelopmentTest.PLATFORMS_SECTION);
        for (String mspec : mspecs) {
            ExecutionEnvironment env = NativeExecutionTestSupport.getTestExecutionEnvironment(mspec);
            for (String dir : dirs) {
                File file = new File(dir);
                if (env == null) {
                    tests.add(TestSuite.warning("Can't get execution environment for " + mspec));
                } else if (!file.exists() || !file.canRead() || !file.isDirectory()) {
                    tests.add(TestSuite.warning("Can't read directory  " + file.getAbsolutePath()));
                } else {
                    tests.add(new MeasurementTestCase("test_upload_first", env, file,   "first    "));
                    tests.add(new MeasurementTestCase("test_upload_first", env, file,   "unchanged"));
                    tests.add(new MeasurementTestCase("test_upload_changed", env, file, "changed  "));
                }
            }
        }
        RemoteDevelopmentTest suite = new MeasurementTestSuite(MeasurementTestCase.class.getName(), tests);
        return suite;
    }
}

