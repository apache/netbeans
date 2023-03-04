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
package org.netbeans.modules.performance.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.netbeans.jemmy.JemmyProperties;

/**
 * Test that measures detailed memory usage. (not reporting anything from here,
 * the test has to call reportDetailMemoryUsage() in one of its methods!)
 * <br>
 * <br>If during measurement exception arise - test fails and no value is
 * reported as Performance Data.
 * <p>
 * Each test should reset the state in {@link close()} method.</p>
 * Test case with implemented Memory Footprint Performance Tests support stuff.
 *
 * @author anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public abstract class MemoryFootprintTestCase extends PerformanceTestCase {

    /**
     * Used platform.
     */
    private static String platform;

    /**
     * IDE process PID.
     */
    private static String pid;

    /**
     * Platforms
     */
    private static final String UNIX = "unix";
    private static final String WINDOWS = "windows";
    private static final String MAC = "mac";
    private static final String UNKNOWN = "unknown";

    /**
     * Specify supported platforms
     */
    private static final String[][] SUPPORTED_PLATFORMS = {
        {"Linux,i386", UNIX},
        {"SunOS,sparc", UNIX},
        {"SunOS,x86", UNIX},
        {"Windows_NT,x86", WINDOWS},
        {"Windows_2000,x86", WINDOWS},
        {"Windows_XP,x86", WINDOWS},
        {"Windows_95,x86", WINDOWS},
        {"Windows_98,x86", WINDOWS},
        {"Windows_Me,x86", WINDOWS},
        {"Windows_Vista,x86", WINDOWS},
        {"Mac_OS_X,ppc", MAC}
    };

    /**
     * Prefix used for the reported performance data as the first part of the
     * name
     */
    protected String prefix;

    /**
     * Creates a new instance of MemoryFootprintTestCase
     *
     * @param testName name of the test
     */
    public MemoryFootprintTestCase(String testName) {
        super(testName);
    }

    /**
     * Creates a new instance of MemoryFootprintTestCase
     *
     * @param testName name of the test
     * @param performanceDataName name for measured performance data, measured
     * values are stored to results under this name
     */
    public MemoryFootprintTestCase(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    /**
     * Set default timeouts
     */
    @Override
    public void initialize() {
        // prolong timeouts
        JemmyProperties.setCurrentTimeout("JMenuOperator.PushMenuTimeout", 60000);
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 60000);
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNodeVisibleTimeout", 60000);
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 5 * 60000);
        JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 5 * 60000);
    }

    /**
     * Get platform on which the code is executed.
     *
     * @return platform identification string
     */
    private String getPlatform() {

        String platformString = (System.getProperty("os.name", "") + "," + System.getProperty("os.arch", "")).replace(' ', '_');
        for (String[] supPlatform : SUPPORTED_PLATFORMS) {
            if (platformString.equalsIgnoreCase(supPlatform[0])) {
                return supPlatform[1];
            }
        }

        log("Unknown platform: " + platformString);
        return UNKNOWN;
    }

    /**
     * Test that measures detailed memory usage.
     * <br>
     * <br>If during measurement exception arise - test fails and no value is
     * reported as Performance Data.
     * <p>
     * Each test should reset the state in {@link close()} method.</p>
     */
    public void testMeasureMemoryFootprint() {
        Exception exceptionDuringMeasurement = null;

        MeasuredMemoryValue[] measuredValues = new MeasuredMemoryValue[repeat_memory + 1];

        useTwoOrderTypes = false;

        checkScanFinished(); // just to be sure, that during measurement we will not wait for scanning dialog

        initialize();

        log("Repeat = " + repeat_memory);

        for (int i = 1; i <= repeat_memory && exceptionDuringMeasurement == null; i++) {
            try {
                testedComponentOperator = null;

                prepare();

                testedComponentOperator = open();

                // Full GC several times
                runGC(5);

                // measure memory footprint
                measuredValues[i] = measureFootprint();

                //report measured values
                measuredValues[i].reportPerformanceData(prefix, i);

            } catch (Exception exc) { // catch for prepare(), open()
                exc.printStackTrace(getLog());
                exceptionDuringMeasurement = exc;
                getScreenshot("exception_during_open");
            } finally {
                try {
                    close();
                    closeAllModal();
                } catch (Exception e) {
                    e.printStackTrace(getLog());
                    getScreenshot("measure");
                    if (exceptionDuringMeasurement == null) {
                        exceptionDuringMeasurement = e;
                    }
                } finally { // finally for initialize(), shutdown(), closeAllDialogs()
                    //TODO export results?
                }
            }
        }

        try {
            shutdown();
            closeAllDialogs();
        } catch (Exception e) {
            e.printStackTrace(getLog());
            getScreenshot("shutdown");
            if (exceptionDuringMeasurement == null) {
                exceptionDuringMeasurement = e;
            }
        } finally {
        }

        if (exceptionDuringMeasurement != null) {
            throw new Error("Exception rises during measurement:" + exceptionDuringMeasurement, exceptionDuringMeasurement);
        }
    }

    /**
     * Measure Footprint
     *
     * @return measured values
     */
    private MeasuredMemoryValue measureFootprint() {
        MeasuredMemoryValue mv = new MeasuredMemoryValue();

        platform = getPlatform();
        pid = getPID();

        log("Platform=" + platform);

        if (platform.equals(UNIX)) {
            getRssVszOnUnix(mv);
        } else if (platform.equals(WINDOWS)) {
            getRssVszOnWindows(mv);
        } //TODO not implemented else if (platform.equals(MAC))
        //TODO not implemented     return psOnMac();
        else {
            fail("Unsupported platform!");
        }

        getHeapAndPermGen(mv);

        getLoadedUnloadedClasses(mv);

        return mv;
    }

    /**
     * Get PID from the {xtest.workdir}/ide.pid file.
     *
     * @return
     */
    private String getPID() {
        String xtestWorkdir = System.getProperty("xtest.workdir");

        if (xtestWorkdir == null) {
            fail("xtest.workdir property is not specified");
        }

        File ideRunning = new File(xtestWorkdir, "ide.pid");
        if (!ideRunning.exists()) {
            fail("Cannot find file containing PID of running IDE (" + ideRunning.getAbsolutePath());
        }

        try {
            LineNumberReader reader = new LineNumberReader(new FileReader(ideRunning));
            String pid = reader.readLine().trim();
            log("PID = " + pid);
            return pid;
        } catch (Exception exc) {
            exc.printStackTrace(getLog());
            fail("Exception rises when reading PID from ide.pid file");
        }
        return "";
    }

    /**
     * Get RSS and VSZ from the output on Windows
     * <pre>
     * ./pslist -m $PID | tail -1 |
     * read NAME0 PID0 VM0 WS WSPK0 PRIV REST0
     * $WS = RSS
     * $PRIV = VSZ
     * </pre>
     *
     * @param mv measured values
     */
    private void getRssVszOnWindows(MeasuredMemoryValue mv) {
        String xtestHome = System.getProperty("xtest.tmpdir");
        String pslist;
        String platformString = (System.getProperty("os.name", "") + "," + System.getProperty("os.arch", "")).replace(' ', '_');
        if (platformString.equalsIgnoreCase(SUPPORTED_PLATFORMS[8][0])) { // This is Vista
            log("Windows platform = " + platformString);
            log("Execiting ps_vista...");
            pslist = executeNativeCommand(xtestHome + "/ps_vista.exe -m " + pid);
        } else {
            log("Windows platform = " + platformString);
            log("Execiting pslist...");
            pslist = executeNativeCommand(xtestHome + "/pslist.exe -m " + pid);
        }
        String pslist_line = getLine(pslist);
        mv.rss = getItem(pslist_line, 3);
        mv.vsz = getItem(pslist_line, 5);
    }

    /**
     * Get RSS and VSZ from the output on Unixes
     * <pre>
     * ps -o rss -p $PID | tail -1 |
     * read RSS
     * ps -o vsz -p $PID | tail -1 |
     * read VSZ
     * </pre>
     *
     * @param mv measured values
     */
    private void getRssVszOnUnix(MeasuredMemoryValue mv) {
        String ps_rss = executeNativeCommand("ps -o rss -p " + pid);
        String ps_vsz = executeNativeCommand("ps -o vsz -p " + pid);
        mv.rss = getLine(ps_rss).trim();
        mv.vsz = getLine(ps_vsz).trim();
    }

    /**
     * Get Heap utilization/cappacity and PermGen utilization/cappacity from the
     * output
     * <pre>
     * jstat -gc $PID | tail -1 |
     * read S0C S1C S0U S1U EC EU OC OU PC PU YGC YGCT FGC FGCT GCT
     * $S0U+$S1U+$EU+$OU  =  heap utilization
     * $S0C+$S1C+$EC+$OC  =  heap capacity
     * $PU  =  utilization of permgen
     * $PC  =  capacity of permgen
     * </pre>
     *
     * @param mv measured values
     */
    private void getHeapAndPermGen(MeasuredMemoryValue mv) {
        String jstat = executeNativeCommand(getJavaBinDirectory() + "jstat -gc " + pid);

        String jstat_line = getLine(jstat);

        int heapU = (int) Math.floor(getItemFloat(jstat_line, 2) + getItemFloat(jstat_line, 3)
                + getItemFloat(jstat_line, 5) + getItemFloat(jstat_line, 7));
        int heapC = (int) Math.floor(getItemFloat(jstat_line, 0) + getItemFloat(jstat_line, 1)
                + getItemFloat(jstat_line, 4) + getItemFloat(jstat_line, 6));

        mv.heap_used = Integer.toString(heapU);
        mv.heap_commited = Integer.toString(heapC);
        mv.permGen_used = Integer.toString((int) Math.floor(getItemFloat(jstat_line, 9)));
        mv.permGen_commited = Integer.toString((int) Math.floor(getItemFloat(jstat_line, 8)));
    }

    /**
     * convert output from jstat with dependency on locale/number format
     */
    private float getItemFloat(String line, int item) {
        try {
            java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
            return nf.parse(getItem(line, item)).floatValue();
        } catch (java.text.ParseException exc) {
            exc.printStackTrace(getLog());
            return -1;
        }
    }

    /**
     * Get locaded/unlodaded classes from the output
     * <pre>
     * jstat -class $PID | tail -1 |
     * read L BL U BU BY TI
     * $L  =  loaded classes
     * $U  =  unloaded classes
     * </pre>
     *
     * @param mv measured values
     */
    private void getLoadedUnloadedClasses(MeasuredMemoryValue mv) {
        String jstat = executeNativeCommand(getJavaBinDirectory() + "jstat -class " + pid);
        String jstat_line = getLine(jstat);
        mv.classes_loaded = getItem(jstat_line, 0);
        mv.classes_unloaded = getItem(jstat_line, 2);
    }

    /**
     * Parse line
     *
     * @param string line to be parsed
     * @return parsed line
     */
    private String getLine(String string) {
        log("Parsed output ==========\n" + string + "\n======");

        int line = (string.length() < 1 ? 0 : string.split("\n").length) - 1;
        return string.split("\n")[line];
    }

    /**
     * Get item with specified number from the line
     *
     * @param line line to be parsed
     * @param item indx of the item
     * @return token with index of item
     */
    private String getItem(String line, int item) {
        String[] lineitems = line.split(" ");
        for (int i = 0, j = 0; i < lineitems.length; i++) {
            if (lineitems[i].trim().length() > 0) {
                if (item == j) {
                    return lineitems[i];
                }
                j++;
            }
        }
        return "N/A";
    }

    /**
     * Get java directory
     *
     * @return {java}/bin
     */
    private String getJavaBinDirectory() {
        String separator = System.getProperty("file.separator");
        String javaDir = System.getProperty("java.home");
        if (javaDir.endsWith(separator + "jre")) {
            javaDir = javaDir.substring(0, javaDir.length() - 4);
        }
        return javaDir + separator + "bin" + separator;
    }

    /**
     * Execute native command
     *
     * @param commandLine command line
     * @return output of the executed command
     */
    private String executeNativeCommand(String commandLine) {

        log("Execute command: [" + commandLine + "].");

        try {
            Process proc = Runtime.getRuntime().exec(commandLine);
            proc.waitFor();

            StringBuilder buffer = new StringBuilder();
            BufferedReader dataInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;

            while ((line = dataInput.readLine()) != null) {
                buffer.append(line);
                buffer.append('\n');
            }

            return buffer.toString();

        } catch (InterruptedException ie) {
            ie.printStackTrace(getLog());
            log("InterruptedException: " + ie.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace(getLog());
            log("None output from command, exception arise " + ioe.toString());
        }
        return null;
    }

    /**
     * All values we are measuring in Memory Footprint tests in one data
     * structure
     */
    public final class MeasuredMemoryValue {

        String rss;                   // RSS
        String vsz;                   // VSZ
        String heap_used;             // Heap used
        String heap_commited;         // Heap commited
        String permGen_used;          // PermGen used
        String permGen_commited;      // PermGen commited
        String classes_loaded;        // classes loaded
        String classes_unloaded;      // classes unloaded

        public MeasuredMemoryValue() {
        }

        /**
         * Report all measured performance data
         *
         * @param prefix prefix has to be used to distinguish different tests
         * @param runOrder order od the run
         */
        public void reportPerformanceData(String prefix, int runOrder) {
            reportPerformance(prefix + " Footprint-RSS", Long.valueOf(rss).longValue(), "kB", runOrder);
            reportPerformance(prefix + " Footprint-VSZ", Long.valueOf(vsz).longValue(), "kB", runOrder);
            reportPerformance(prefix + " Heap-Used", Long.valueOf(heap_used).longValue(), "kB", runOrder);
            reportPerformance(prefix + " Heap-Commited", Long.valueOf(heap_commited).longValue(), "kB", runOrder);
            reportPerformance(prefix + " PermGen-Used", Long.valueOf(permGen_used).longValue(), "kB", runOrder);
            reportPerformance(prefix + " PermGen-Commited", Long.valueOf(permGen_commited).longValue(), "kB", runOrder);
            reportPerformance(prefix + " Classes-Loaded", Long.valueOf(classes_loaded).longValue(), "", runOrder);
            reportPerformance(prefix + " Classes-Unloaded", Long.valueOf(classes_unloaded).longValue(), "", runOrder);
        }

    }
}
