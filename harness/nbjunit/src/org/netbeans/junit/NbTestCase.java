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

package org.netbeans.junit;

import java.awt.EventQueue;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import junit.framework.TestResult;
import org.junit.AssumptionViolatedException;
import org.junit.Ignore;
import org.netbeans.insane.live.LiveReferences;
import org.netbeans.insane.live.Path;
import org.netbeans.insane.scanner.CountingVisitor;
import org.netbeans.insane.scanner.ScannerUtils;
import org.netbeans.junit.diff.Diff;
import org.netbeans.junit.internal.MemoryPreferencesFactory;
import org.netbeans.junit.internal.NbModuleLogHandler;
/**
 * NetBeans extension to JUnit's {@link TestCase}.
 * Adds various abilities such as comparing golden files, getting a working
 * directory for test files, testing memory usage, etc.
 */

public abstract class NbTestCase extends TestCase implements NbTest {
    static {
        MethodOrder.initialize();
        System.setProperty("bootstrap.disableJDKCheck", "true");
    }
    /**
     * active filter
     */
    private Filter filter;
    /** the amount of time the test was executing for
     */
    private long time;
    /** our working directory */
    private String workDirPath;
    
    
    /**
     * Constructs a test case with the given name.
     * Normally you will just use:
     * <pre>
     * public class MyTest extends NbTestCase {
     *     public MyTest(String name) {super(name);}
     *     public void testWhatever() {...}
     * }
     * </pre>
     * @param name name of the test case
     */
    public NbTestCase(String name) {
        super(name);
    }
    
    
    /**
     * Sets active filter.
     * @param filter Filter to be set as active for current test, null will reset filtering.
     */
    public @Override void setFilter(Filter filter) {
        this.filter = filter;
    }
    
    /**
     * Returns expected fail message.
     * @return expected fail message if it's expected this test fail, null otherwise.
     */
    public @Override String getExpectedFail() {
        if (filter == null) {
            return null;
        }
        return filter.getExpectedFail(this.getName());
    }
    
    /**
     * Checks if a test isn't filtered out by the active filter.
     * @return true if the test can run
     */
    public @Override boolean canRun() {
        if (getClass().isAnnotationPresent(Ignore.class)) {
            String message = getClass().getAnnotation(Ignore.class).value();
            System.err.println("Skipping " + getClass().getName() + (message.isEmpty() ? "" : ": " + message));
            return false;
        }
        
        try {
            if (getClass().getMethod(getName()).isAnnotationPresent(Ignore.class)) {
                String message = getClass().getMethod(getName()).getAnnotation(Ignore.class).value();
                System.err.println("Skipping " + getClass().getName() + "." + getName() + (message.isEmpty() ? "" : ": " + message));
                return false;
            }
        } catch (NoSuchMethodException x) {
            // Specially named methods; let it pass.
        }
        
        if (NbTestSuite.ignoreRandomFailures()) {
            if (getClass().isAnnotationPresent(RandomlyFails.class)) {
                System.err.println("Skipping " + getClass().getName());
                return false;
            }
            try {
                if (getClass().getMethod(getName()).isAnnotationPresent(RandomlyFails.class)) {
                    System.err.println("Skipping " + getClass().getName() + "." + getName());
                    return false;
                }
            } catch (NoSuchMethodException x) {
                // Specially named methods; let it pass.
            }
        }
        if (null == filter) {
            //System.out.println("NBTestCase.canRun(): filter == null name=" + name ());
            return true; // no filter was aplied
        }
        boolean isIncluded = filter.isIncluded(this.getName());
        //System.out.println("NbTestCase.canRun(): filter.isIncluded(this.getName())="+isIncluded+" ; this="+this);
        return isIncluded;
    }
    
    /**
     * Provide ability for tests, setUp and tearDown to request that they run only in the AWT event queue.
     * By default, false.
     * @return true to run all test methods, setUp and tearDown in the EQ, false to run in whatever thread
     */
    protected boolean runInEQ() {
        return false;
    }
    
    private static final long vmDeadline;
    static {
        boolean debugMode = false;

        // check if we are debugged
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        List<String> args = runtime.getInputArguments();
        if (args.contains("-Xdebug") || args.contains("-agentlib:jdwp")) { //NOI18N
            debugMode = true;
        } else {
            for (String arg : args) {
                if (arg.startsWith("-agentlib:jdwp=")) {
                    debugMode = true;
                    break;
                }
            }
        }
        Integer vmTimeRemaining = Integer.getInteger("nbjunit.hard.timeout");
        if (vmTimeRemaining != null && !debugMode) {
            vmDeadline = System.currentTimeMillis() + vmTimeRemaining;
        } else {
            vmDeadline = -1L;
        }
    }
    
    private static ThreadLocal<Boolean> DEFAULT_TIME_OUT_CALLED = new ThreadLocal<Boolean>();
    /** Provides support for tests that can have problems with terminating.
     * Runs the test in a "watchdog" that measures the time the test shall
     * take and if it does not terminate it reports a failure including a thread dump.
     * <p>If the system property {@code nbjunit.hard.timeout} is set to a number
     * (of milliseconds) by which the whole VM must exit (as in the {@code timeout}
     * property to Ant's {@code <junit>} task),
     * this "soft timeout" will default to some portion of the remaining time, so
     * that we can capture a meaningful thread dump rather than simply report that the
     * test took too long. (If the VM crashes, the hard timeout kicks in.) Otherwise the
     * default is 0 (no soft timeout). For an Ant-based NBM project, {@code common.xml}
     * specifies 600000 (ten minutes) as a default hard timeout, and sets the system
     * property, so soft timeouts should be the default.
     * @return amount ms to give one test to finish or 0 to disable time outs
     * @since 1.20
     */
    protected int timeOut() {
        DEFAULT_TIME_OUT_CALLED.set(true);
        return 0;
    }
    private int computeTimeOut() {
        if (vmDeadline == -1L) {
            return 0;
        }
        Boolean prev = DEFAULT_TIME_OUT_CALLED.get();
        try {
            DEFAULT_TIME_OUT_CALLED.set(null);
            int tm = timeOut();
            if (!Boolean.TRUE.equals(DEFAULT_TIME_OUT_CALLED.get())) {
                return tm;
            }
        } finally {
            DEFAULT_TIME_OUT_CALLED.set(prev);
        }
        
        int remaining = (int) (vmDeadline - System.currentTimeMillis());
        if (remaining > 1500) {
            return (remaining - 1000) / 2;
        }
        return 1500;
    }

    /**
    * Allows easy collecting of log messages send thru java.util.logging API.
    * Overwrite and return the log level to collect logs to logging file. 
    * If the method returns non-null level, then the level is assigned to
    * the <code>Logger.getLogger({@linkplain #logRoot logRoot()})</code> and the messages reported to it
    * are then send into regular log file (which is accessible thru {@link NbTestCase#getLog})
    * and in case of failure the last few messages is also included
    * in <code>failure.getMessage()</code>.
    *
    * @return default implementation returns <code>null</code> which disables any logging
    *   support in test
    * @since 1.27
    * @see Log#enable
    */
    protected Level logLevel() {
        return null;
    }
         
    /**
     * If overriding {@link #logLevel}, may override this as well to collect messages from only some code.
     * @return {@code ""} (default) to collect messages from all loggers; or {@code "my.pkg"} or {@code "my.pkg.Class"} etc.
     * @since 1.68
     */
    protected String logRoot() {
        return "";
    }
         
    /**
     * Runs the test case, while conditionally skip some according to result of
     * {@link #canRun} method.
     */
    @Override
    public void run(final TestResult result) {
        if (canRun()) {
            System.setProperty("netbeans.full.hack", "true"); // NOI18N
            System.setProperty("java.util.prefs.PreferencesFactory",
                    MemoryPreferencesFactory.class.getName());//NOI18N
            try {
                Preferences.userRoot().sync();
            } catch(BackingStoreException bex) {}
            Level lev = logLevel();
            if (lev != null) {
                Log.configure(lev, logRoot(), NbTestCase.this);
            }
            super.run(result);
        }
    }

    private static StringBuilder printThreadInfo(ThreadInfo ti, StringBuilder sb) {
        // print thread information
        printThread(ti, sb);

        // print stack trace with locks
        StackTraceElement[] stacktrace = ti.getStackTrace();
        MonitorInfo[] monitors = ti.getLockedMonitors();
        for (int i = 0; i < stacktrace.length; i++) {
            StackTraceElement ste = stacktrace[i];
            sb.append("\t at " + ste.toString()).append("\n");
            for (MonitorInfo mi : monitors) {
                if (mi.getLockedStackDepth() == i) {
                    sb.append("\t  - locked " + mi).append("\n");
                }
            }
        }
        sb.append("\n");
        return sb;
    }

    private static StringBuilder printThread(ThreadInfo ti, StringBuilder sb) {
        sb.append("\"" + ti.getThreadName() + "\"" + " Id="
                + ti.getThreadId() + " in " + ti.getThreadState());
        if (ti.getLockName() != null) {
            sb.append(" waiting on lock=" + ti.getLockName());
        }
        if (ti.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (ti.isInNative()) {
            sb.append(" (running in native)");
        }
        sb.append("\n");
        if (ti.getLockOwnerName() != null) {
            sb.append("\t owned by " + ti.getLockOwnerName() + " Id="
                    + ti.getLockOwnerId()).append("\n");
        }
        return sb;
    }

    private static void printMonitorInfo(ThreadInfo ti, MonitorInfo[] monitors, StringBuilder sb) {
        sb.append("\tLocked monitors: count = " + monitors.length).append("\n");
        for (MonitorInfo mi : monitors) {
            sb.append("\t  - " + mi + " locked at ").append("\n");
            sb.append("\t      " + mi.getLockedStackDepth() + " "
                    + mi.getLockedStackFrame()).append("\n");
        }
    }

    private static void printLockInfo(LockInfo[] locks, StringBuilder sb) {
        sb.append("\tLocked synchronizers: count = " + locks.length).append("\n");
        for (LockInfo li : locks) {
            sb.append("\t  - " + li).append("\n");
        }
        sb.append("\n");
    }

    private static String threadDump() {
        ThreadMXBean tmx = ManagementFactory.getPlatformMXBean(ThreadMXBean.class);

        ThreadInfo[] threads = tmx.dumpAllThreads(tmx.isSynchronizerUsageSupported(), tmx.isObjectMonitorUsageSupported());
        StringBuilder sb = new StringBuilder();

        for (ThreadInfo ti : threads) {
            printThreadInfo(ti, sb);
        }
        
        long[] lockedThreads = tmx.isSynchronizerUsageSupported() ? tmx.findDeadlockedThreads() : null;
        long[] monitorLockedThreads = tmx.findMonitorDeadlockedThreads();

        if (lockedThreads != null) {
            sb.append("\n================\nDead-locked threads:\n");
            ThreadInfo[] infos = tmx.getThreadInfo(lockedThreads, true, tmx.isObjectMonitorUsageSupported());
            for (ThreadInfo ti : infos) {
                printThreadInfo(ti, sb);
                printLockInfo(ti.getLockedSynchronizers(), sb);
                sb.append("\n");
            }
        } else if (monitorLockedThreads != null) {
            ThreadInfo[] infos = tmx.getThreadInfo(monitorLockedThreads, Integer.MAX_VALUE);
            for (ThreadInfo ti : infos) {
                // print thread information
                printThread(ti, sb);
                printMonitorInfo(ti, ti.getLockedMonitors(), sb);
            }
        }
        return sb.toString();
    }

    
    /**
     * Runs the bare test sequence. It checks {@link #runInEQ} and possibly 
     * schedules the call of <code>setUp</code>, <code>runTest</code> and <code>tearDown</code>
     * to AWT event thread. It also consults {@link #timeOut} and if so, it starts a 
     * count down and aborts the <code>runTest</code> if the time out expires.
     * @exception Throwable if any exception is thrown
     */
    @Override
    public void runBare() throws Throwable {
        abstract class Guard implements Runnable {
            private boolean finished;
            private Throwable t;

            public abstract void doSomething() throws Throwable;
            
            public @Override void run() {
                try {
                    doSomething();
                } catch (Throwable thrwn) {
                    if (MethodOrder.isShuffled()) {
                        thrwn = Log.wrapWithAddendum(thrwn, "(executed in shuffle mode, run with -DNbTestCase.order=" + MethodOrder.getSeed() + " to reproduce the order)", true);
                    }
                    this.t = Log.wrapWithMessages(thrwn, getWorkDirPath());
                } finally {
                    synchronized (this) {
                        finished = true;
                        notifyAll();
                    }
                }
            }
            
            public synchronized void waitFinished() throws Throwable {
                waitFinished(0);
            }

            public synchronized void waitFinished(final int timeout) throws Throwable {
                long time = timeout;
                long startTime = System.currentTimeMillis();
                while (!finished){
                    try {
                        wait(time);
                        if (timeout > 0) {
                            time = timeout - (System.currentTimeMillis() - startTime);
                            if (time < 1) {
                                break;
                            }
                        }
                    } catch (InterruptedException ex) {
                        if (t == null) {
                            t = ex;
                        }
                    }
                }
                if (t != null) {
                    throw t;
                }

                if (!finished) {
                    throw Log.wrapWithMessages(new AssertionFailedError ("The test " + getName() + " did not finish in " + timeout + "ms\n" +
                        threadDump())
                    , getWorkDirPath());
                }
            }
        }
        /* original sequence from TestCase.runBare():
            setUp();
            try {
                runTest();
            } finally {
                tearDown();
            }
         */
        // setUp
        if(runInEQ()) {
            Guard setUp = new Guard() {
                public @Override void doSomething() throws Throwable {
                    setUp();
                }
            };
            EventQueue.invokeLater(setUp);
            // need to have timeout because previous test case can block AWT thread
            setUp.waitFinished(computeTimeOut());
        } else {
            try {
                setUp();
            } catch (AssumptionViolatedException ex) {
                // ignore, the test is assumed to be meaningless.
                return;
            }
        }
        try {
            // runTest
            Guard runTest = new Guard() {
                public @Override void doSomething() throws Throwable {
                    long now = System.nanoTime();
                    try {
                        runTest();
                    } catch (AssumptionViolatedException ex) {
                        // ignore, the test is assumed to be meaningless.
                    } catch (Throwable t) {
                        noteWorkDir(workdirNoCreate());
                        throw noteRandomness(t);
                    } finally {
                        long last = System.nanoTime() - now;
                        if (last < 1) {
                            last = 1;
                        }
                        NbTestCase.this.time = last;
                    }
                }
            };
            if (runInEQ()) {
                EventQueue.invokeLater(runTest);
                runTest.waitFinished(computeTimeOut());
            } else {
                if (computeTimeOut() == 0) {
                    // Regular test.
                    runTest.run();
                    runTest.waitFinished();
                } else {
                    // Regular test with time out
                    Thread watchDog = new Thread(runTest, "Test Watch Dog: " + getName());
                    watchDog.start();
                    runTest.waitFinished(computeTimeOut());
                }
            }
        } finally {
            // tearDown
            if(runInEQ()) {
                Guard tearDown = new Guard() {
                    public @Override void doSomething() throws Throwable {
                        tearDown();
                    }
                };
                EventQueue.invokeLater(tearDown);
                // need to have timeout because test can block AWT thread
                tearDown.waitFinished(computeTimeOut());
            } else {
                tearDown();
            }
        }
    }
    /**
     * Make a note of the working directory for a failed test.
     * If running inside Hudson, archive it and show the presumed artifact location.
     */
    private void noteWorkDir(File wd) {
        if (!wd.isDirectory()) {
            return;
        }
        try {
            String buildURL = System.getenv("BUILD_URL");
            if (buildURL != null) {
                String workspace = new File(System.getenv("WORKSPACE")).getCanonicalPath();
                if (!workspace.endsWith(File.separator)) {
                    workspace += File.separator;
                }
                String path = wd.getCanonicalPath();
                if (path.startsWith(workspace)) {
                    copytree(wd, new File(wd.getParentFile(), wd.getName() + "-FAILED"));
                    System.err.println("Working directory: " + buildURL + "artifact/" +
                            path.substring(workspace.length()).replace(File.separatorChar, '/') + "-FAILED/");
                    return;
                }
            }
            System.err.println("Working directory: " + wd);
        } catch (Exception x) {
            x.printStackTrace(); // do not mask real error
        }
    }
    static void copytree(File from, File to) throws IOException {
        if (from.isDirectory()) {
            if (!to.mkdirs()) {
                throw new IOException("mkdir: " + to);
            }
            for (File f : from.listFiles()) {
                copytree(f, new File(to, f.getName()));
            }
        } else {
            InputStream is = new FileInputStream(from);
            try {
                OutputStream os = new FileOutputStream(to);
                try {
                    // XXX using FileChannel would be more efficient, but more complicated
                    BufferedInputStream bis = new BufferedInputStream(is);
                    BufferedOutputStream bos = new BufferedOutputStream(os);
                    int c;
                    while ((c = bis.read()) != -1) {
                        bos.write(c);
                    }
                    bos.flush();
                    bos.close();
                } finally {
                    os.close();
                }
            } finally {
                is.close();
            }
        }
    }
    private Throwable noteRandomness(Throwable t) {
        Class<?> c = getClass();
        if (c.isAnnotationPresent(RandomlyFails.class)) {
            return Log.wrapWithAddendum(t, "(" + c.getSimpleName() + " marked @RandomlyFails so try just running test again)", false);
        }
        try {
            if (c.getMethod(getName()).isAnnotationPresent(RandomlyFails.class)) {
                return Log.wrapWithAddendum(t, "(" + c.getSimpleName() + "." + getName() + " marked @RandomlyFails so try just running test again)", false);
            }
        } catch (NoSuchMethodException x) {}
        return t;
        // XXX would be nice to actually try to rerun the test (but would make runBare more complicated)
    }

    /** Parses the test name to find out whether it encodes a number. The
     * testSomeName1343 represents number 1343.
     * @return the number
     */
    protected final int getTestNumber() {
        try {
            Matcher m = Pattern.compile("test[a-zA-Z]*([0-9]+)").matcher(getName());
            assertTrue("Name does not contain numbers: " + getName(), m.find());
            return Integer.valueOf(m.group(1)).intValue();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Name: " + getName() + " does not represent number");
            return 0;
        }
    }
    
    
    /** in nanoseconds */
    final long getExecutionTime() {
        return time;
    }
    
    // additional asserts !!!!
    
    
    /**
     * Asserts that two files are the same (their content is identical), when files
     * differ {@link org.netbeans.junit.AssertionFileFailedError AssertionFileFailedError} exception is thrown.
     * Depending on the Diff implementation additional output can be generated to the file/dir specified by the
     * <b>diff</b> param.
     * @param message the detail message for this assertion
     * @param test first file to be compared, by the convention this should be the test-generated file
     * @param pass second file to be compared, it should be so called 'golden' file, which defines
     * the correct content for the test-generated file.
     * @param diff file, where differences will be stored, when null differences will not be stored. In case
     * it points to directory the result file name is constructed from the <b>pass</b> argument and placed to that
     * directory. Constructed file name consists from the name of pass file (without extension and path) appended
     * by the '.diff'.
     * @param externalDiff instance of class implementing the {@link org.netbeans.junit.diff.Diff} interface, it has to be
     * already initialized, when passed in this assertFile function.
     */
    public static void assertFile(String message, String test, String pass, String diff, Diff externalDiff) {
        Diff diffImpl = null == externalDiff ? Manager.getSystemDiff() : externalDiff;
        File    diffFile = getDiffName(pass, null == diff ? null : new File(diff));
        
        if (null == diffImpl) {
            fail("diff is not available");
        } else {
            try {
                if (null == diffFile) {
                    if (diffImpl.diff(test, pass, null)) {
                        throw new AssertionFileFailedError(message, "");
                    }
                } else {
                    if (diffImpl.diff(test, pass, diffFile.getAbsolutePath())) {
                        throw new AssertionFileFailedError(message, diffFile.getAbsolutePath());
                    }
                }
            } catch (IOException e) {
                fail("exception in assertFile : " + e.getMessage());
            }
        }
    }
    /**
     * Asserts that two files are the same, it uses specific {@link org.netbeans.junit.diff.Diff Diff} implementation to
     * compare two files and stores possible differences in the output file.
     * @param test first file to be compared, by the convention this should be the test-generated file
     * @param pass second file to be compared, it should be so called 'golden' file, which defines the
     * correct content for the test-generated file.
     * @param diff file, where differences will be stored, when null differences will not be stored. In case
     * it points to directory the result file name is constructed from the <b>pass</b> argument and placed to that
     * directory. Constructed file name consists from the name of pass file (without extension and path) appended
     * by the '.diff'.
     * @param externalDiff instance of class implementing the {@link org.netbeans.junit.diff.Diff} interface, it has to be
     * already initialized, when passed in this assertFile function.
     */
    public static void assertFile(String test, String pass, String diff, Diff externalDiff) {
        assertFile(null, test, pass, diff, externalDiff);
    }
    /**
     * Asserts that two files are the same, it compares two files and stores possible differences
     * in the output file, the message is displayed when assertion fails.
     * @param message the detail message for this assertion
     * @param test first file to be compared, by the convention this should be the test-generated file
     * @param pass second file to be compared, it should be so called 'golden' file, which defines the
     * correct content for the test-generated file.
     * @param diff file, where differences will be stored, when null differences will not be stored. In case
     * it points to directory the result file name is constructed from the <b>pass</b> argument and placed to that
     * directory. Constructed file name consists from the name of pass file (without extension and path) appended
     * by the '.diff'.
     */
    public static void assertFile(String message, String test, String pass, String diff) {
        assertFile(message, test, pass, diff, null);
    }
    /**
     * Asserts that two files are the same, it compares two files and stores possible differences
     * in the output file.
     * @param test first file to be compared, by the convention this should be the test-generated file
     * @param pass second file to be compared, it should be so called 'golden' file, which defines the
     * correct content for the test-generated file.
     * @param diff file, where differences will be stored, when null differences will not be stored. In case
     * it points to directory the result file name is constructed from the <b>pass</b> argument and placed to that
     * directory. Constructed file name consists from the name of pass file (without extension and path) appended
     * by the '.diff'.
     */
    public static void assertFile(String test, String pass, String diff) {
        assertFile(null, test, pass, diff, null);
    }
    /**
     * Asserts that two files are the same, it just compares two files and doesn't produce any additional output.
     * @param test first file to be compared, by the convention this should be the test-generated file
     * @param pass second file to be compared, it should be so called 'golden' file, which defines the
     * correct content for the test-generated file.
     */
    public static void assertFile(String test, String pass) {
        assertFile(null, test, pass, null, null);
    }
    
    /**
     * Asserts that two files are the same (their content is identical), when files
     * differ {@link org.netbeans.junit.AssertionFileFailedError AssertionFileFailedError} exception is thrown.
     * Depending on the Diff implementation additional output can be generated to the file/dir specified by the
     * <b>diff</b> param.
     * @param message the detail message for this assertion
     * @param test first file to be compared, by the convention this should be the test-generated file
     * @param pass second file to be compared, it should be so called 'golden' file, which defines
     * the correct content for the test-generated file.
     * @param diff file, where differences will be stored, when null differences will not be stored. In case
     * it points to directory the result file name is constructed from the <b>pass</b> argument and placed to that
     * directory. Constructed file name consists from the name of pass file (without extension and path) appended
     * by the '.diff'.
     * @param externalDiff instance of class implementing the {@link org.netbeans.junit.diff.Diff} interface, it has to be
     * already initialized, when passed in this assertFile function.
     */
    public static void assertFile(String message, File test, File pass, File diff, Diff externalDiff) {
        Diff diffImpl = null == externalDiff ? Manager.getSystemDiff() : externalDiff;
        File    diffFile = getDiffName(pass.getAbsolutePath(), diff);
        
        /*
        System.out.println("NbTestCase.assertFile(): diffFile="+diffFile);
        System.out.println("NbTestCase.assertFile(): diffImpl="+diffImpl);
        System.out.println("NbTestCase.assertFile(): externalDiff="+externalDiff);
         */
        
        if (null == diffImpl) {
            fail("diff is not available");
        } else {
            try {
                if (diffImpl.diff(test, pass, diffFile)) {
                    throw new AssertionFileFailedError(message+"\n diff: "+diffFile, null == diffFile ? "" : diffFile.getAbsolutePath());
                }
            } catch (IOException e) {
                fail("exception in assertFile : " + e.getMessage());
            }
        }
    }
    /**
     * Asserts that two files are the same, it uses specific {@link org.netbeans.junit.diff.Diff Diff} implementation to
     * compare two files and stores possible differences in the output file.
     * @param test first file to be compared, by the convention this should be the test-generated file
     * @param pass second file to be compared, it should be so called 'golden' file, which defines the
     * correct content for the test-generated file.
     * @param diff file, where differences will be stored, when null differences will not be stored. In case
     * it points to directory the result file name is constructed from the <b>pass</b> argument and placed to that
     * directory. Constructed file name consists from the name of pass file (without extension and path) appended
     * by the '.diff'.
     * @param externalDiff instance of class implementing the {@link org.netbeans.junit.diff.Diff} interface, it has to be
     * already initialized, when passed in this assertFile function.
     */
    public static void assertFile(File test, File pass, File diff, Diff externalDiff) {
        assertFile("Difference between " + test + " and " + pass, test, pass, diff, externalDiff);
    }
    /**
     * Asserts that two files are the same, it compares two files and stores possible differences
     * in the output file, the message is displayed when assertion fails.
     * @param message the detail message for this assertion
     * @param test first file to be compared, by the convention this should be the test-generated file
     * @param pass second file to be compared, it should be so called 'golden' file, which defines the
     * correct content for the test-generated file.
     * @param diff file, where differences will be stored, when null differences will not be stored. In case
     * it points to directory the result file name is constructed from the <b>pass</b> argument and placed to that
     * directory. Constructed file name consists from the name of pass file (without extension and path) appended
     * by the '.diff'.
     */
    public static void assertFile(String message, File test, File pass, File diff) {
        assertFile(message, test, pass, diff, null);
    }
    /**
     * Asserts that two files are the same, it compares two files and stores possible differences
     * in the output file.
     * @param test first file to be compared, by the convention this should be the test-generated file
     * @param pass second file to be compared, it should be so called 'golden' file, which defines the
     * correct content for the test-generated file.
     * @param diff file, where differences will be stored, when null differences will not be stored. In case
     * it points to directory the result file name is constructed from the <b>pass</b> argument and placed to that
     * directory. Constructed file name consists from the name of pass file (without extension and path) appended
     * by the '.diff'.
     */
    public static void assertFile(File test, File pass, File diff) {
        assertFile("Difference between " + test + " and " + pass, test, pass, diff, null);
    }
    /**
     * Asserts that two files are the same, it just compares two files and doesn't produce any additional output.
     * @param test first file to be compared, by the convention this should be the test-generated file
     * @param pass second file to be compared, it should be so called 'golden' file, which defines the
     * correct content for the test-generated file.
     */
    public static void assertFile(File test, File pass) {
        assertFile("Difference between " + test + " and " + pass, test, pass, null, null);
    }
    
    /**
     */
    private static File getDiffName(String pass, File diff) {
        if (null == diff) {
            return null;
        }
        
        if (!diff.exists() || diff.isFile()) {
            return diff;
        }
        
        StringBuilder d = new StringBuilder();
        int i1, i2;
        
        d.append(diff.getAbsolutePath());
        i1 = pass.lastIndexOf('\\');
        i2 = pass.lastIndexOf('/');
        i1 = i1 > i2 ? i1 : i2;
        i1 = -1 == i1 ? 0 : i1 + 1;
        
        i2 = pass.lastIndexOf('.');
        i2 = -1 == i2 ? pass.length() : i2;
        
        if (0 < d.length()) {
            d.append("/");
        }
        
        d.append(pass.substring(i1, i2));
        d.append(".diff");
        return new File(d.toString());
    }
    
    // methods for work with tests' workdirs
    
    
    /** Returns path to test method working directory as a String. Path is constructed
     * as ${nbjunit.workdir}/${package}.${classname}/${testmethodname}. (The nbjunit.workdir
     * property should be set in junit.properties; otherwise the default is ${java.io.tmpdir}/tests.)
     * Please note that this method does not guarantee that the working directory really exists.
     * @return a path to a test method working directory
     */
    public String getWorkDirPath() {
        if (workDirPath != null) {
            return workDirPath;
        }
        
        String name = getName();
        // start - PerformanceTestCase overrides getName() method and then
        // name can contain illegal characters
        String osName = System.getProperty("os.name");
        if (osName != null && osName.startsWith("Windows")) {
            char ntfsIllegal[] ={'"','/','\\','?','<','>','|',':'};
            for (int i=0; i<ntfsIllegal.length; i++) {
                name = name.replace(ntfsIllegal[i], '~');
            }
        }
        // end
        
        // #94319 - shorten workdir path if the following is too long
        // "Manager.getWorkDirPath()+File.separator+getClass().getName()+File.separator+name"
        int len1 = Manager.getWorkDirPath().length();
        String clazz = getClass().getName();
        int len2 = clazz.length();
        int len3 = name.length();
        
        int tooLong = Integer.getInteger("nbjunit.too.long", 100);
        if (len1 + len2 + len3 > tooLong) {
            clazz = abbrevDots(clazz);
            len2 = clazz.length();
        }

        if (len1 + len2 + len3 > tooLong) {
            name = abbrevCapitals(name);
        }
        
        String p = Manager.getWorkDirPath() + File.separator + clazz + File.separator + name;
        String realP;
        
        for (int i = 0; ; i++) {
            realP = i == 0 ? p : p + "-" + i;
            if (usedPaths.add(realP)) {
                break;
            }
        }
        
        
        workDirPath = realP;
        return realP;
    }

    private static Set<String> usedPaths = new HashSet<String>();
    
    private static String abbrevDots(String dotted) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (String item : dotted.split("\\.")) {
            sb.append(sep);
            sb.append(item.charAt(0));
            sep = ".";
        }
        return sb.toString();
    }

    private static String abbrevCapitals(String name) {
        if (name.startsWith("test")) {
            name = name.substring(4);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            if (Character.isUpperCase(name.charAt(i))) {
                sb.append(Character.toLowerCase(name.charAt(i)));
            }
        }
        if (sb.length() == 0) {
            // for names without uppercase (e.g. test12345, test_a)
            return name;
        } else {
            return sb.toString();
        }
    }

    private File workdirNoCreate() {
        return Manager.normalizeFile(new File(getWorkDirPath()));
    }
    
    /** Returns unique working directory for a test (each test method has a unique dir).
     * If not available, method tries to create it. This method uses {@link #getWorkDirPath}
     * method to determine the unique path.
     * <p><strong>Warning:</strong> the working directory is <em>not</em> guaranteed
     * to be empty when you get it, so if this is being called in {@link #setUp} you
     * are strongly advised to first call {@link #clearWorkDir} to ensure that each
     * test run starts with a clean slate.</p>
     * @throws IOException if the directory cannot be created
     * @return file to the working directory directory
     */
    public File getWorkDir() throws IOException {
        // construct path from workdir classpath + classname + methodname
        
        /*
        String path = this.getClass().getResource("").getFile().toString();
        String srcElement="src";
        String workdirElement="workdir";
        int srcStart = path.lastIndexOf(srcElement);
        // base path
        path = path.substring(0,srcStart)+workdirElement;
        // package+class
        path += "/"+this.getClass().getName().replace('.','/');
        // method name
        path += "/"+getName();
         */
        
        // new way how to get path - from defined property + classname +methodname
        
        
        
        // now we have path, so if not available, create workdir
        File workdir = workdirNoCreate();
        if (workdir.exists()) {
            if (!workdir.isDirectory()) {
                // work dir exists, but is not directory - this should not happen
                // trow exception
                throw new IOException("workdir exists, but is not a directory, workdir = " + workdir);
            } else {
                // everything looks correctly, return the path
                return workdir;
            }
        } else {
            // we need to create it
            boolean result = workdir.mkdirs();
            if (result == false) {
                // mkdirs() failed - throw an exception
                throw new IOException("workdir creation failed: " + workdir);
            } else {
                // everything looks ok - return path
                return workdir;
            }
        }
    }
    
    // private method for deleting a file/directory (and all its subdirectories/files)
    private static void deleteFile(File file) throws IOException {
        Files.walkFileTree(file.toPath(), new SimpleFileVisitor<java.nio.file.Path>() {
            @Override
            public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(java.nio.file.Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    // private method for deleting every subfiles/subdirectories of a file object
    static void deleteSubFiles(File file) throws IOException {
        File files[] = file.getCanonicalFile().listFiles();
        if (files != null) {
            for (File f : files) {
                deleteFile(f);
            }
        } else {
            // probably do nothing - file is not a directory
        }
    }

    /** Deletes all files including subdirectories in test's working directory.
     * @throws IOException if any problem has occured during deleting files/directories
     */
    public void clearWorkDir() throws IOException {
        synchronized (logStreamTable) {
            File workdir = getWorkDir();
            closeAllStreams();
            deleteSubFiles(workdir);
        }
    }
    
    private String lastTestMethod=null;
    
    private boolean hasTestMethodChanged() {
        if (!this.getName().equals(lastTestMethod)) {
            lastTestMethod=this.getName();
            return true;
        } else {
            return false;
        }
    }
    
    // hashtable holding all already used logs and correspondig printstreams
    private final Map<String,PrintStream> logStreamTable = new HashMap<String,PrintStream>();
    
    private PrintStream getFileLog(String logName) throws IOException {
        synchronized (logStreamTable) {
            if (hasTestMethodChanged()) {
                // we haven't used logging capability - create hashtables
                closeAllStreams();
            } else {
                if (logStreamTable.containsKey(logName)) {
                    //System.out.println("Getting stream from cache:"+logName);
                    return logStreamTable.get(logName);
                }
            }
            // we didn't used this log, so let's create it
            OutputStream fileLog = new WFOS(new File(getWorkDir(),logName));
            PrintStream printStreamLog = new PrintStream(fileLog,true);
            logStreamTable.put(logName,printStreamLog);
            //System.out.println("Created new stream:"+logName);
            return printStreamLog;
        }
    }
    
    private void closeAllStreams() {
        for (PrintStream ps : logStreamTable.values()) {
            ps.close();
        }
        logStreamTable.clear();
    }

    private static class WFOS extends FilterOutputStream {
        private File f;
        private int bytes;
        
        public WFOS(File f) throws FileNotFoundException {
            super(new FileOutputStream(f));
            this.f = f;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            add(len);
            out.write(b, off, len);
        }

        @Override
        public void write(byte[] b) throws IOException {
            add(b.length);
            out.write(b);
        }

        @Override
        public void write(int b) throws IOException {
            add(1);
            out.write(b);
        }

        private synchronized void add(int i) throws IOException {
            bytes += i;
            if (bytes >= 1048576L) { // 1mb
                out.close();
                File trim = new File(f.getParent(), "TRIMMED_" + f.getName());
                trim.delete();
                f.renameTo(trim);
                f.delete();
                out = new FileOutputStream(f);
                bytes = 0;
            }
        }
        
        
    } // end of WFOS
    
    // private PrintStream wrapper for System.out
    PrintStream systemOutPSWrapper = new PrintStream(System.out);
    
    /** Returns named log stream. If log cannot be created as a file in the
     * testmethod working directory, PrintStream created from System.out is used. Please
     * note, that tests shoudn't call log.close() method, unless they really don't want
     * to use this log anymore.
     * @param logName name of the log - file in the working directory
     * @return Log PrintStream
     */
    public PrintStream getLog(String logName) {
        try {
            return getFileLog(logName);
        } catch (IOException ioe) {
            /// hey, file is not available - log will be made to System.out
            // we should probably write a little note about it
            //System.err.println("Test method "+this.getName()+" - cannot open file log to file:"+logName
            //                                +" - defaulting to System.out");
            return systemOutPSWrapper;
        }
    }
    
    /** Return default log named as ${testmethod}.log. If the log cannot be created
     * as a file in testmethod working directory, PrinterStream to System.out is returned
     * @return log
     */
    public PrintStream getLog() {
        return getLog(this.getName()+".log");
    }
    
    /** Simple and easy to use method for printing a message to a default log
     * @param message meesage to log
     */
    public void log(String message) {
        getLog().println(message);
    }
    
    
    /** Easy to use method for logging a message to a named log
     * @param log which log to use
     * @param message message to log
     */
    public void log(String log, String message) {
        getLog(log).println(message);
    }
    
    // reference file stuff ...
    
    
    /** Get PrintStream to log inteded for reference files comparision. Reference
     * log is stored as a file named ${testmethod}.ref in test method working directory.
     * If the file cannot be created, the testcase will automatically fail.
     * @return PrintStream to referencing log
     */
    public PrintStream getRef() {
        String refFilename = this.getName()+".ref";
        try {
            return getFileLog(refFilename);
        } catch (IOException ioe) {
            // canot get ref file - return system.out
            //System.err.println("Test method "+this.getName()+" - cannot open ref file:"+refFilename
            //                                +" - defaulting to System.out and failing test");
            fail("Could not open reference file: "+refFilename);
            return  systemOutPSWrapper;
        }
    }
    
    /** Easy to use logging method for printing a message to a reference log.
     * @param message message to log
     */
    public void ref(String message) {
        getRef().println(message);
    }
    
    /** Get the test method specific golden file from ${xtest.data}/goldenfiles/${classname}
     * directory. If not found, try also deprecated src/data/goldenfiles/${classname}
     * resource directory.
     * @param filename filename to get from golden files directory
     * @return golden file
     */
    public File getGoldenFile(String filename) {
        String fullClassName = this.getClass().getName();
        String goldenFileName = fullClassName.replace('.', '/')+"/"+filename;
        // golden files are in ${xtest.data}/goldenfiles/${classname}/...
        File goldenFile = new File(getDataDir()+"/goldenfiles/"+goldenFileName);
        if(goldenFile.exists()) {
            // Return if found, otherwise try to find golden file in deprecated
            // location. When deprecated part is removed, add assertTrue(goldenFile.exists())
            // instead of if clause.
            return goldenFile;
        }
        
        /** Deprecated - this part is deprecated */
        // golden files are in data/goldenfiles/${classname}/* ...
        String className = fullClassName;
        int lastDot = fullClassName.lastIndexOf('.');
        if (lastDot != -1) {
            className = fullClassName.substring(lastDot+1);
        }
        goldenFileName = className+"/"+filename;
        URL url = this.getClass().getResource("data/goldenfiles/"+goldenFileName);
        assertNotNull("Golden file not found in any of the following locations:\n  "+
                goldenFile+"\n  "+
                "src/"+fullClassName.replace('.', '/').substring(0, fullClassName.indexOf(className))+"data/goldenfiles/"+goldenFileName,
                url);
        String resString = convertNBFSURL(url);
        goldenFile = new File(resString);
        return goldenFile;
        /** Deprecated end. */
    }
    
    /** Returns pointer to directory with test data (golden files, sample files, ...).
     * It is the same from xtest.data property.
     * @return data directory
     */
    public File getDataDir() {
        // XXX should this be deprecated?
        String xtestData = System.getProperty("xtest.data");
        if(xtestData != null) {
            return Manager.normalizeFile(new File(xtestData));
        } else {
            // property not set => try to find it
            URL codebase = getClass().getProtectionDomain().getCodeSource().getLocation();
            if (!codebase.getProtocol().equals("file")) {
                throw new Error("Cannot find data directory from " + codebase);
            }
            File dataDir;
            try {
                dataDir = new File(new File(codebase.toURI()).getParentFile(), "data");
            } catch (URISyntaxException x) {
                throw new Error(x);
            }
            return Manager.normalizeFile(dataDir);
        }
    }
    
    /** Get the default testmethod specific golden file from
     * data/goldenfiles/${classname}/${testmethodname}.pass
     * @return filename to get from golden files resource directory
     */
    public File getGoldenFile() {
        return getGoldenFile(this.getName()+".pass");
    }
    
    
    /** Compares golden file and reference log. If both files are the
     * same, test passes. If files differ, test fails and diff file is
     * created (diff is created only when using native diff, for details
     * see JUnit module documentation)
     * @param testFilename reference log file name
     * @param goldenFilename golden file name
     * @param diffFilename diff file name (optional, if null, then no diff is created)
     */
    public void compareReferenceFiles(String testFilename, String goldenFilename, String diffFilename) {
        try {
            if (!getRef().equals(systemOutPSWrapper)) {
                // better flush the reference file
                getRef().flush();
                getRef().close();
            }
            File goldenFile = getGoldenFile(goldenFilename);
            File testFile = new File(getWorkDir(),testFilename);
            File diffFile = new File(getWorkDir(),diffFilename);
            String message = "Files differ";
            if(System.getProperty("xtest.home") == null) {
                // show location of diff file only when run without XTest (run file in IDE)
                message += "; check "+diffFile;
            }
            assertFile(message, testFile, goldenFile, diffFile);
        } catch (IOException ioe) {
            fail("Could not obtain working direcory");
        }
    }
    
    /** Compares default golden file and default reference log. If both files are the
     * same, test passes. If files differ, test fails and default diff (${methodname}.diff)
     * file is created (diff is created only when using native diff, for details
     * see JUnit module documentation)
     */
    public void compareReferenceFiles() {
        compareReferenceFiles(this.getName()+".ref",this.getName()+".pass",this.getName()+".diff");
    }
    
    // utility stuff for getting resources from NetBeans' filesystems
    
    /** Converts NetBeans filesystem URL to absolute path.
     * @param url URL to convert
     * @return absolute path
     * @deprecated No longer applicable as of NB 4.0 at the latest.
     *            <code>FileObject.getURL()</code> should be returning a <code>file</code>-protocol
     *            URL, which can be converted to a disk path using <code>new File(URI)</code>; or
     *            use <code>FileUtil.toFile</code>.
     */
    @Deprecated
    public static String convertNBFSURL(URL url) {
        if(url == null) {
            throw new IllegalArgumentException("Given URL should not be null.");
        }
        String externalForm = url.toExternalForm();
        if (externalForm.startsWith("nbfs://")) {
            // new nbfsurl format (post 06/2003)
            return convertNewNBFSURL(url);
        } else {
            // old nbfsurl (and non nbfs urls)
            return convertOldNBFSURL(url);
        }
    }
    
    // radix for new nbfsurl
    private static final int radix = 16;
    // new nbfsurl decoder - assumes the external form
    // begins with nbfs://
    private static String convertNewNBFSURL(URL url) {
        String externalForm = url.toExternalForm();
        String path;
        if (externalForm.startsWith("nbfs://nbhost/")) {
            // even newer nbfsurl (hope it does not change soon)
            // return path and omit first slash sign
            path = url.getPath().substring(1);
        } else {
            path = externalForm.substring("nbfs://".length());
        }
        // convert separators (%2f = /,  etc.)
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int len = path.length();
        while (i < len) {
            char ch = path.charAt(i++);
            if (ch == '%' && (i+1) < len) {
                char h1 = path.charAt(i++);
                char h2 = path.charAt(i++);
                // convert d1+d2 hex number to char
                ch = (char)Integer.parseInt("" + h1 + h2, radix);
                
            }
            sb.append(ch);
        }
        return sb.toString();
        
    }
    
    // old nbfsurl decoder
    private static String convertOldNBFSURL(URL url) {
        String path = url.getFile();
        if(url.getProtocol().equals("nbfs")) {
            // delete prefix of special Filesystem (e.g. org.netbeans.modules.javacvs.JavaCvsFileSystem)
            String prefixFS = "FileSystem ";
            if(path.indexOf(prefixFS)>-1) {
                path = path.substring(path.indexOf(prefixFS)+prefixFS.length());
            }
            // convert separators ("QB="/" etc.)
            StringBuilder sb = new StringBuilder();
            int i = 0;
            int len = path.length();
            while (i < len) {
                char ch = path.charAt(i++);
                if (ch == 'Q' && i < len) {
                    ch = path.charAt(i++);
                    switch (ch) {
                        case 'B':
                            sb.append('/');
                            break;
                        case 'C':
                            sb.append(':');
                            break;
                        case 'D':
                            sb.append('\\');
                            break;
                        case 'E':
                            sb.append('#');
                            break;
                        default:
                            // not a control sequence
                            sb.append('Q');
                            sb.append(ch);
                            break;
                    }
                } else {
                    // not Q
                    sb.append(ch);
                }
            }
            path = sb.toString();
        }
        return path;
    }
    
    
    /** Asserts that the object can be garbage collected. Tries to GC ref's referent.
     * @param text the text to show when test fails.
     * @param ref the referent to object that
     * should be GCed
     */
    public static void assertGC(String text, Reference<?> ref) {
        assertGC(text, ref, Collections.emptySet());
    }
    
    /** Asserts that the object can be garbage collected. Tries to GC ref's referent.
     * @param text the text to show when test fails.
     * @param ref the referent to object that should be GCed
     * @param rootsHint a set of objects that should be considered part of the
     * rootset for this scan. This is useful if you want to verify that one structure
     * (usually long living in real application) is not holding another structure
     * in memory, without setting a static reference to the former structure.
     * <p><strong>Example:</strong></p>
     * <pre>
     *  // test body
     *  WeakHashMap map = new WeakHashMap();
     *  Object target = new Object();
     *  map.put(target, "Val");
     *  
     *  // verification step
     *  Reference ref = new WeakReference(target);
     *  target = null;
     *  assertGC("WeakMap does not hold the key", ref, Collections.singleton(map));
     * </pre>
     */
    public static void assertGC(final String text, final Reference<?> ref, final Set<?> rootsHint) {
        NbModuleLogHandler.whileIgnoringOOME(new Runnable() {
            @SuppressWarnings({"SleepWhileHoldingLock", "SleepWhileInLoop"})
            public @Override void run() {
        List<byte[]> alloc = new ArrayList<byte[]>();
        int size = 100000;
        for (int i = 0; i < 50; i++) {
            if (ref.get() == null) {
                return;
            }
            try {
                System.gc();
            } catch (OutOfMemoryError error) {
                // OK
            }
            try {
                System.runFinalization();
            } catch (OutOfMemoryError error) {
                // OK
            }
            try {
                alloc.add(new byte[size]);
                size = (int)(((double)size) * 1.3);
            } catch (OutOfMemoryError error) {
                size = size / 2;
            }
            try {
                if (i % 3 == 0) {
                    Thread.sleep(321);
                }
            } catch (InterruptedException t) {
                // ignore
            }
        }
        alloc = null;
        String str = null;
        try {
            str = findRefsFromRoot(ref.get(), rootsHint);
        } catch (Exception e) {
            throw new AssertionFailedErrorException(e);
        } catch (OutOfMemoryError err) {
            // OK
        }
        fail(text + ":\n" + str);
            }
        });
    }
    
    /** Assert size of some structure. Traverses the whole reference
     * graph of objects accessible from given root object and check its size
     * against the limit.
     * @param message the text to show when test fails.
     * @param limit maximal allowed heap size of the structure
     * @param root the root object from which to traverse
     */
    public static void assertSize(String message, int limit, Object root ) {
        assertSize(message, Arrays.asList( new Object[] {root} ), limit);
    }
    
    /** Assert size of some structure. Traverses the whole reference
     * graph of objects accessible from given roots and check its size
     * against the limit.
     * @param message the text to show when test fails.
     * @param roots the collection of root objects from which to traverse
     * @param limit maximal allowed heap size of the structure
     */
    public static void assertSize(String message, Collection<?> roots, int limit) {
        assertSize(message, roots, limit, new Object[0]);
    }
    
    /** Assert size of some structure. Traverses the whole reference
     * graph of objects accessible from given roots and check its size
     * against the limit.
     * @param message the text to show when test fails.
     * @param roots the collection of root objects from which to traverse
     * @param limit maximal allowed heap size of the structure
     * @param skip Array of objects used as a boundary during heap scanning,
     *        neither these objects nor references from these objects
     *        are counted.
     */
    public static void assertSize(String message, Collection<?> roots, int limit, Object[] skip) {
        org.netbeans.insane.scanner.Filter f = ScannerUtils.skipObjectsFilter(Arrays.asList(skip), false);
        assertSize(message, roots, limit, f);
    }
    
    
    /** Assert size of some structure. Traverses the whole reference
     * graph of objects accessible from given roots and check its size
     * against the limit.
     * @param message the text to show when test fails.
     * @param roots the collection of root objects from which to traverse
     * @param limit maximal allowed heap size of the structure
     * @param skip custom filter for counted objects
     * @return actual size or <code>-1</code> on internal error.
     */
    public static int assertSize(String message, Collection<?> roots, int limit, final MemoryFilter skip) {
        org.netbeans.insane.scanner.Filter f = new org.netbeans.insane.scanner.Filter() {
            public @Override boolean accept(Object o, Object refFrom, Field ref) {
                return !skip.reject(o);
            }
        };
        return assertSize(message, roots, limit, f);
    }

    private static int assertSize(String message, Collection<?> roots, int limit,
            org.netbeans.insane.scanner.Filter f) {
        try {
            final CountingVisitor counter = new CountingVisitor();
            ScannerUtils.scan(f, counter, roots, false);
            int sum = counter.getTotalSize();
            if (sum > limit) {
                StringBuilder sb = new StringBuilder(4096);
                sb.append(message);
                sb.append(": leak ").append(sum - limit).append(" bytes ");
                sb.append(" over limit of ");
                sb.append(limit).append(" bytes");
                Set<Class<?>> classes = new TreeSet<Class<?>>(new Comparator<Class<?>>() {
                    public @Override int compare(Class<?> c1, Class<?> c2) {
                        int r = counter.getSizeForClass(c2) - counter.getSizeForClass(c1);
                        return r != 0 ? r : c1.hashCode() - c2.hashCode();
                    }
                });
                classes.addAll(counter.getClasses());
                for (Class<?> cls : classes) {
                    if (counter.getCountForClass(cls) == 0) {
                        continue;
                    }
                    sb.append("\n  ").append(cls.getName()).append(": ").
                            append(counter.getCountForClass(cls)).append(", ").
                            append(counter.getSizeForClass(cls)).append("B");
                }
                fail(sb.toString());
            }
            return sum;
        } catch (Exception e) {
            throw new AssertionFailedErrorException("Could not traverse reference graph", e);
        }
    }

    /**
     * Fails a test with known bug ID.
     * @param bugID the bug number according bug report system.
     */
    public static void failByBug(int bugID) {
        throw new AssertionKnownBugError(bugID);
    }

    /**
     * Fails a test with known bug ID and with the given message.
     * @param bugID the bug number according bug report system.
     * @param message the text to show when test fails.
     */
    public static void failByBug(int bugID, String message) {
        throw new AssertionKnownBugError(bugID, message);
    }
    
    private static String findRefsFromRoot(final Object target, final Set<?> rootsHint) throws Exception {
        int count = Integer.getInteger("assertgc.paths", 1);
        StringBuilder sb = new StringBuilder();
        final Map<Object,Void> skip = new IdentityHashMap<Object,Void>();
    
        org.netbeans.insane.scanner.Filter knownPath = new org.netbeans.insane.scanner.Filter() {
            public @Override boolean accept(Object obj, Object referredFrom, Field reference) {
                return !skip.containsKey(obj);
}
        };
        
        while (count-- > 0) {
            @SuppressWarnings("unchecked")
            Map<Object,Path> m = LiveReferences.fromRoots(Collections.singleton(target), (Set<Object>)rootsHint, null, knownPath);
            Path p = m.get(target);
            if (p == null) {
                break;
            }
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append(p);
            for (; p != null; p=p.nextNode()) {
                Object o = p.getObject();
                if (o != target) {
                    skip.put(o, null);
                }
            }
        }
        return sb.length() > 0 ? sb.toString() : "Not found!!!";
    }
    
}
