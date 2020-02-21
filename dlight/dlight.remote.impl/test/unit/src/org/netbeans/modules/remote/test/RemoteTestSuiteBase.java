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

package org.netbeans.modules.remote.test;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Timer;
import junit.framework.TestCase;
import junit.framework.TestResult;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.remote.impl.fs.server.FSSTransport;

/**
 *
 */
    public class RemoteTestSuiteBase extends NativeExecutionBaseTestSuite {

    public RemoteTestSuiteBase() {
    }

    public RemoteTestSuiteBase(String name) {
        super(name);
    }

    public RemoteTestSuiteBase(Class<? extends NativeExecutionBaseTestCase>... testClasses) {
        super(testClasses);
    }

    public RemoteTestSuiteBase(String name, String defaultSection) {
        super(name, defaultSection);
    }

    public RemoteTestSuiteBase(String name, String defaultSection, Class<? extends NativeExecutionBaseTestCase>... testClasses) {
        super(name, defaultSection, testClasses);
    }

    @Override
    public final void run(TestResult result) {
        try {
            registerTestSuiteSetup(getName());            
            super.run(result);
        } finally {
            registerTestSuiteTearDown(getName());
        }
    }
    
        
    private static final Map<String, Long> stats = new HashMap<>();
    private static final Object statsLock = new Object();
    static final ThreadLocal<Long> suiteStartTime = new ThreadLocal<>();
    
    private static TimeoutThreadDumper suiteTimeoutThreadDumper;
    private static TimeoutThreadDumper testTimeoutThreadDumper;
    private static final Object dumperLock = new Object();
    protected static final int TEST_THREAD_DUMP_TIMEOUT = Integer.getInteger("remote.test.thread.dump.timeout", 1*60*1000);
    protected static final int SUITE_THREAD_DUMP_TIMEOUT = Integer.getInteger("remote.test.suite.thread.dump.timeout", 8*60*1000);

    public static void registerTestSuiteSetup(String suiteName) {
        synchronized (dumperLock) {
            if (suiteTimeoutThreadDumper != null) {
                suiteTimeoutThreadDumper.stop();
            }
            suiteTimeoutThreadDumper = new TimeoutThreadDumper(SUITE_THREAD_DUMP_TIMEOUT, "Suite " + suiteName);
        }
        suiteStartTime.set(System.currentTimeMillis());
        System.err.printf("### Starting suite %s\n", suiteName);
        clearStats(suiteName);
    }

    public static void registerTestSuiteTearDown(String suiteName) {
        synchronized (dumperLock) {
            if (suiteTimeoutThreadDumper != null) {
                suiteTimeoutThreadDumper.stop();
            }
        }        
        Long tm = suiteStartTime.get();
        long time = (tm == null) ? -1 : System.currentTimeMillis() - tm.longValue();
        printStats(suiteName, time);
    }

    public static void registerTestSetup(TestCase test) {
        synchronized (dumperLock) {
            if (testTimeoutThreadDumper != null) {
                testTimeoutThreadDumper.stop();
            }
            testTimeoutThreadDumper = new TimeoutThreadDumper(TEST_THREAD_DUMP_TIMEOUT, "Test " + testFullName(test));
        }        
        String fullName = testFullName(test);
        registerTestSetup(fullName);
    }
    
    private static void registerTestSetup(String fullName) {
        synchronized (dumperLock) {
            if (testTimeoutThreadDumper != null) {
                testTimeoutThreadDumper.stop();
            }
        }        
        synchronized (statsLock) {
            Long value = stats.get(fullName);
            if (value != null) {
                System.err.printf("### Non-null value for %s ?!\n", fullName);
            }
            stats.put(fullName, Long.valueOf(System.currentTimeMillis()));
        }
        System.err.printf("\n###> setUp    [%s] %s\n", new Date(), fullName);
    }
    
    public static void registerTestTearDown(TestCase test) {
        String fullName = testFullName(test);
        registerTestTearDown(fullName, false);
    }
    
//    private static void registerTestTearDown(String fullName) {
//        registerTestTearDown(fullName, true);
//    }
    
    private static void registerTestTearDown(String fullName, boolean clearThisTestTimer) {
        long time;
        synchronized (statsLock) {
            Long value = stats.get(fullName);
            if (value == null) {
                System.err.printf("### Null value for %s ?!\n", fullName);
                time = 0;
                stats.put(fullName, Long.valueOf(-1));
            } else {
                time = System.currentTimeMillis() - value.longValue();
                stats.put(fullName, Long.valueOf(time));
            }
            if (clearThisTestTimer) {
                stats.remove(fullName);
            }
        }
        System.err.printf("\n###< tearDown [%s] %s; duration: %d seconds\n", new Date(), fullName, time/1000);
    }
    
    private static String testFullName(TestCase test) {
        return test.getClass().getName() + '.' + test.getName();
    }
    
    private static void clearStats(String suiteName) {
        Map<String, Long> statsCopy;
        synchronized (statsLock) {
            statsCopy = new HashMap<>(stats);
            stats.clear();
        }
        if (!statsCopy.isEmpty()) {
            String title = String.format("### Unreported times (%s):\n", suiteName);
            printStats(title, statsCopy);
        }
    }
    
    private static void printStats(String suiteName, long suiteTime) {
        Map<String, Long> statsCopy;
        synchronized (statsLock) {
            statsCopy = new HashMap<>(stats);
        }        
        String title = String.format("\n\n### Test suite %s took %d seconds\n", suiteName, suiteTime/1000);
        printStats(title, statsCopy);
    }    
    
    private static void printStats(String title, Map<String, Long> statsCopy) {
        System.err.printf("%s\n", title);
        ArrayList<Map.Entry<String, Long>> entries = new ArrayList<>(statsCopy.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, Long>>() {
            @Override
            public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                long d = o2.getValue().longValue() - o1.getValue().longValue();
                return d == 0 ? 0 : (d > 0 ? 1 : -1);
            }
        });
        for (Map.Entry<String, Long> entry : entries) {
            System.err.printf("### %s took %d seconds\n", entry.getKey(), entry.getValue().longValue()/1000);
        }
    }    
    
    private static class TimeoutThreadDumper implements ActionListener {

        private final Timer timer;
        private final String testName;

        public TimeoutThreadDumper(int timeout, String testName) {
            this.timer = new Timer(timeout, this);
            this.testName = testName;
            this.timer.start();
        }

        public void stop() {
            this.timer.stop();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            stop();
            String header = "### " + testName + " probably hanged - getting thread dump " + new Date();
            String footer = "### end of " + testName + " thread dump " + new Date();
            System.err.printf("### Dumping fs_server instances\n");
            NativeExecutionTestSupport.threadsDump(header, footer);
            FSSTransport.testDumpInstances(System.err);
        }
    }    
}
