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
package org.netbeans.modules.nativeexecution.api.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;
import junit.framework.Test;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.netbeans.modules.nativeexecution.ConcurrentTasksSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Kvashin
 */
public class HostInfoUtilsTest extends NativeExecutionBaseTestCase {

    public static Test suite() {
        return new NativeExecutionBaseTestSuite(HostInfoUtilsTest.class);
    }

    public HostInfoUtilsTest(String name) {
        super(name);
    }

    public HostInfoUtilsTest(String name, ExecutionEnvironment testEnv) {
        super(name, testEnv);
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
    public void testGetHostInfo() throws Exception {
        // A situation from bugs #202550, #202568

        // One thread is trying to get hostinfo ... 
        final Runnable r1 = new Runnable() {

            @Override
            public void run() {
                try {
                    ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
                    HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                    HostInfoUtils.dumpInfo(hostInfo, System.out);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (CancellationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };

        // While another one tries to create new NativeProcessBuilder at the time
        // when HostInfo is not available yet... 

        final Runnable r2 = new Runnable() {

            @Override
            public void run() {
                try {
                    NativeProcessBuilder npb = NativeProcessBuilder.newLocalProcessBuilder();
                    npb.setExecutable("echo").setArguments("123"); // NOI18N
                    NativeProcess process = npb.call();
                    try {
                        process.waitFor();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    System.out.println(ProcessUtils.readProcessOutputLine(process));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };

        HostInfoUtils.resetHostsData();
        assert !HostInfoUtils.isHostInfoAvailable(ExecutionEnvironmentFactory.getLocal());

        ConcurrentTasksSupport support = new ConcurrentTasksSupport(2);
        support.addFactory(new ConcurrentTasksSupport.TaskFactory() {

            @Override
            public Runnable newTask() {
                return r1;
            }
        });

        support.addFactory(new ConcurrentTasksSupport.TaskFactory() {

            @Override
            public Runnable newTask() {
                return r2;
            }
        });

        support.init();
        support.start();
        support.waitCompletion();
    }

//    @ForAllEnvironments(section = "dlight.nativeexecution.hostinfo")
//    public void testGetOS() {
//        try {
//            System.out.println(HostInfoUtils.getHostInfo(getTestExecutionEnvironment()).getOS().getName());
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (CancellationException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//    }

    @org.junit.Test
    public void testCpuFamily() throws Exception {
        class Pair {
            public Pair(String mspec, HostInfo.CpuFamily cpuFamily) {
                this.mspec = mspec;
                this.cpuFamily = cpuFamily;
            }
            public final String mspec;
            public final HostInfo.CpuFamily cpuFamily;
        }
        Pair[] pairs = new Pair[] {
            new Pair("intel-S2", HostInfo.CpuFamily.X86),
            new Pair("sparc-S2", HostInfo.CpuFamily.SPARC),
            new Pair("intel-Linux", HostInfo.CpuFamily.X86)
//            new Pair("intel-FreeBSD", HostInfo.CpuFamily.X86)
        };
        for (Pair pair : pairs) {
            ExecutionEnvironment env = NativeExecutionTestSupport.getTestExecutionEnvironment(pair.mspec);
            if (env == null) {
                System.err.printf("Warning: execution environment not found for mspec %s\n", pair.mspec);
            } else {
                ConnectionManager.getInstance().connectTo(env);
                HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                assertEquals("CPU family for " + env, pair.cpuFamily, hostInfo.getCpuFamily());
            }
        }
    }

    /**
     * This test assures that only first call to getHostInfo does the job.
     * So any subsequent call should return cached result.
     * Also it assures that HostInfoUtils.isHostInfoAvailable() works fast and
     * correct.
     *
     * Note: in case of some error during getHostInfo() the result should not be
     * stored in a cache. So subsequent calls WILL initiate data re-fetching.
     */
    @org.junit.Test
    public void testMultipleGetInfo() {
        final ExecutionEnvironment local = ExecutionEnvironmentFactory.getLocal();

        System.setProperty("dlight.nativeexecution.SlowHostInfoProviderEnabled", "true"); // NOI18N

        try {
            // Reset hosts data - it may be already collected in previous tests

            HostInfoUtils.resetHostsData();

            assertFalse(HostInfoUtils.isHostInfoAvailable(local));

            Thread fetchingThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        HostInfoUtils.getHostInfo(local);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (CancellationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });

            fetchingThread.start();

            // As SlowHostInfoProviderEnabled we know that fetching info will
            // take at least 3 seconds. From the other hand it should not take
            // more than 30 seconds (as we are on the localhost).

            int count = 60;

            while (!HostInfoUtils.isHostInfoAvailable(local)) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }

                if (--count == 0) {
                    break;
                }
            }

            try {
                fetchingThread.interrupt();
                fetchingThread.join();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }

            assertTrue("HostInfo MUST be available already (30 seconds passed)", count > 0); // NOI18N
            assertTrue("HostInfo cannot be available already (only " + ((60 - count) / 2) + " seconds passed)", count < 55); // NOI18N

            long startTime = System.currentTimeMillis();
            HostInfoUtils.dumpInfo(HostInfoUtils.getHostInfo(local), System.out);
            long endTime = System.currentTimeMillis();

            assertTrue((endTime - startTime) / 1000 < 2);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (CancellationException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            System.setProperty("dlight.nativeexecution.SlowHostInfoProviderEnabled", "false"); // NOI18N
        }
    }

//    public void testGetInfoInterrupting() {
//        System.setProperty("dlight.nativeexecution.SlowHostInfoProviderEnabled", "true"); // NOI18N
//
//        final ExecutionEnvironment local = ExecutionEnvironmentFactory.getLocal();
//        try {
//            HostInfoUtils.resetHostsData();
//
//            assertFalse(HostInfoUtils.isHostInfoAvailable(local));
//
//            Thread fetchingThread = new Thread(new Runnable() {
//
//                public void run() {
//                    try {
//                        HostInfoUtils.getHostInfo(local);
//                    } catch (IOException ex) {
//                        Exceptions.printStackTrace(ex);
//                    } catch (CancellationException ex) {
//                        Exceptions.printStackTrace(ex);
//                    }
//                }
//            });
//
//            fetchingThread.start();
//
//            fetchingThread.interrupt();
//
//            try {
//                fetchingThread.join();
//            } catch (InterruptedException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//
//            System.out.println(HostInfoUtils.isHostInfoAvailable(local));
//            try {
//                HostInfoUtils.dumpInfo(HostInfoUtils.getHostInfo(local), System.out);
//            } catch (IOException ex) {
//                Exceptions.printStackTrace(ex);
//            } catch (CancellationException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//
//        } finally {
//            System.setProperty("dlight.nativeexecution.SlowHostInfoProviderEnabled", "false"); // NOI18N
//        }
//    }
//    @Test
//    public void testGetPlatformLocal() throws Exception {
//        ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
//        HostInfo info = HostInfoUtils.getHostInfo(env);
//        String os_name = System.getProperty("os.name").toLowerCase();
//
//        switch(platform.getOSType()) {
//            case GENUNIX:
//                assertTrue(Utilities.isUnix());
//                break;
//            case LINUX:
//                assertTrue(os_name.contains("linux"));
//                break;
//            case MACOSX:
//                assertTrue(os_name.contains("mac") && Utilities.isMac());
//                break;
//            case SOLARIS:
//                assertTrue(os_name.contains("sunos"));
//                break;
//            case WINDOWS:
//                assertTrue(os_name.contains("windows") && Utilities.isWindows());
//                break;
//        }
//        String os_arch = System.getProperty("os.arch");
//        switch (platform.getHardwareType()) {
//            case SPARC:
//                assertTrue(os_arch.contains("sparc"));
//                break;
//            case X86:
//                assertTrue(os_arch.contains("86"));
//                break;
//        }
//    }
//    @Test
//    public void testGetPlatformRemote() throws Exception {
//        testGetPlatform("my_host", "my_login", 22, "pwd", "SOLARIS", "86");
//    }
//    private void testGetPlatform(String host, String user, int port, String passwd,
//            String osTypeShouldContain, String hardwareTypeShouldContain) throws Exception {
//        ExecutionEnvironment env = ExecutionEnvironmentFactory.createNew(user, host, port);
//        ConnectionManager.getInstance().connectTo(env, passwd.toCharArray(), true);
//        Platform platform = HostInfoUtils.getPlatform(env);
//        assertTrue(platform.getHardwareType().toString().contains(hardwareTypeShouldContain));
//        assertTrue(platform.getOSType().toString().contains(osTypeShouldContain));
//    }
    @ForAllEnvironments(section = "dlight.nativeexecution.hostinfo")
    public void testRemoteSearchFile() throws Exception {

        ExecutionEnvironment env = getTestExecutionEnvironment();
        System.out.println("Test testRemoteSearchFile(" + env.toString() + ")"); // NOI18N

        HostInfo info = HostInfoUtils.getHostInfo(env);
        assertNotNull(info);

        HostInfoUtils.dumpInfo(info, System.out);
        assertNotNull(info.getShell());

        String testDir = info.getTempDir() + "/some dir"; // NOI18N
        String testFileName = "some (" + new Random().nextInt() + ") file"; // NOI18N
        System.out.println("Use file '" + testFileName + "' in '" + testDir + "' for testing"); // NOI18N

        int mkDirResult = CommonTasksSupport.mkDir(env, testDir, null).get();
        assertTrue(mkDirResult == 0);

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
        npb.setExecutable("touch").setArguments(testDir + "/" + testFileName); // NOI18N
        int touchResult = npb.call().waitFor();
        assertTrue(touchResult == 0);

        try {
            String result = null;
            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrong Path", testDir, "/usr/bin"), testFileName, true); // NOI18N
            assertNotNull(result);
            assertEquals(result, testDir + "/" + testFileName); // NOI18N

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath", "/bin", "/usr/bin"), "rm", true); // NOI18N
            assertNotNull(result);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath", "/bin", "/usr/bin"), "rm", false); // NOI18N
            assertNotNull(result);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath"), "ls", true); // NOI18N
            assertNotNull(result);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath"), "ls", false); // NOI18N
            assertNull(result);
        } finally {
            CommonTasksSupport.rmDir(env, testDir, true, new PrintWriter(System.err));
        }

    }

    public void testUnixSearchFile() throws Exception {
        System.out.println("Test testUnixSearchFile()"); // NOI18N

        ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
        HostInfo info = HostInfoUtils.getHostInfo(env);

        assertNotNull(info);

        if (info.getShell() == null) {
            return;
        }

        if (!info.getOSFamily().isUnix()) {
            System.out.println("Skipped on " + info.getOSFamily().name()); // NOI18N
            return;
        }

        File testDir = new File(info.getTempDirFile(), "some dir"); // NOI18N
        testDir.mkdir();
        File testFile = File.createTempFile("some (", ") file", testDir); // NOI18N
        testFile.createNewFile();

        System.out.println("Use file '" + testFile.getCanonicalPath() + "' for testing"); // NOI18N

        try {
            String result = null;
            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrong Path", testDir.getCanonicalPath(), "/usr/bin"), testFile.getName(), true); // NOI18N
            assertNotNull(result);

            String expectedPath = testFile.getCanonicalPath();

            if (info.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
                expectedPath = WindowsSupport.getInstance().convertToShellPath(result);
            }

            assertEquals(expectedPath, result);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath", "/bin", "/usr/bin"), "rm", true); // NOI18N
            assertNotNull(result);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath", "/bin", "/usr/bin"), "rm", false); // NOI18N
            assertNotNull(result);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath"), "ls", true); // NOI18N
            assertNotNull(result);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath"), "ls", false); // NOI18N
            assertNull(result);
        } finally {
            testFile.delete();
            testDir.delete();
        }
    }

    public void testWindowsSearchFile() throws Exception {
        System.out.println("Test testWindowsSearchFile()"); // NOI18N

        ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
        HostInfo info = HostInfoUtils.getHostInfo(env);

        assertNotNull(info);

        if (info.getOSFamily() != HostInfo.OSFamily.WINDOWS) {
            System.out.println("Skipped on " + info.getOSFamily().name()); // NOI18N
            return;
        }

        final File testDir = new File(info.getTempDirFile(), "some dir"); // NOI18N

        if (!testDir.exists()) {
            boolean mkdirResult = testDir.mkdir();
            assertTrue("Failed to create directory " + testDir, mkdirResult); // NOI18N
        }

        final File testFile = File.createTempFile("some (", ") file", testDir); // NOI18N

        assertTrue("Cannot create test file in " + testDir, testFile.exists() && testFile.canWrite()); // NOI18N

        System.out.println("Use file '" + testFile.getCanonicalPath() + "' for testing"); // NOI18N

        try {
            String result;

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath", "c:\\Windows", testDir.getCanonicalPath()), testFile.getName(), true); // NOI18N
            assertNotNull(result);

            assertEquals(testFile.getCanonicalPath(), result);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath", "c:\\Windows", "c:\\Windows\\system32"), "cmd.exe", true); // NOI18N
            assertNotNull(result);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath", "c:\\Windows"), "cmd.exe", true); // NOI18N
            assertNotNull(result);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath", "c:\\Windows\\system32"), "cmd.exe", false); // NOI18N
            assertNotNull(result);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath", "c:\\Windows"), "cmd.exe", false); // NOI18N
            assertNull(result);

            result = HostInfoUtils.searchFile(env, Arrays.asList("/wrongPath"), "wrongFile", true); // NOI18N
            assertNull(result);
        } finally {
            testFile.delete();
            testDir.delete();
        }
    }
//    private void testLoggers() {
//        Logger logger = Logger.getAnonymousLogger();
//        logger.setLevel(Level.FINEST);
//        logger.fine("fff");
//        logger.finest("FFFFfff");
//    }
}
