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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Measure startup time by org.netbeans.core.perftool.StartLog. Number of starts
 * with new userdir is defined by property
 * <br> <code> org.netbeans.performance.repeat.with.new.userdir </code>
 * <br> and number of starts with old userdir is defined by property
 * <br> <code> org.netbeans.performance.repeat </code> Run measurement defined
 * number times, but forget first measured value, it's a attempt to have still
 * the same testing conditions with loaded and cached files.
 *
 * @author Martin.Brehovsky@sun.com, Antonin.Nebuzelsky@sun.com,
 * Marian.Mirilovic@sun.com
 */
public class MeasureStartupTimeTestCase extends org.netbeans.junit.NbPerformanceTestCase {

    /**
     * String used for unix platform.
     */
    protected static final String UNIX = "unix";

    /**
     * String used for windows platform.
     */
    protected static final String WINDOWS = "windows";

    /**
     * String used for mac platform.
     */
    protected static final String MAC = "mac";

    /**
     * String used for unknown platform.
     */
    protected static final String UNKNOWN = "unknown";

    /**
     * Number of repeated runs on the same userdir (for first run is used new
     * userdir).
     */
    protected static int repeat = Integer.getInteger("org.netbeans.performance.repeat", 1).intValue();

    /**
     * Number of repeated runs on new userdir.
     */
    protected static int repeatNewUserdir = Integer.getInteger("org.netbeans.performance.repeat.with.new.userdir", 1).intValue();

    /**
     * Number used for unknown time.
     */
    protected static final long UNKNOWN_TIME = -1;

    /**
     * Table of the supported platforms.
     */
    protected static final String[][] SUPPORTED_PLATFORMS = {
        {"Linux,i386", UNIX},
        {"Linux,amd64", UNIX},
        {"SunOS,sparc", UNIX},
        {"SunOS,x86", UNIX},
        {"Windows_NT,x86", WINDOWS},
        {"Windows_NT,amd64", WINDOWS},
        {"Windows_2000,x86", WINDOWS},
        {"Windows_2000,amd64", WINDOWS},
        {"Windows_XP,x86", WINDOWS},
        {"Windows_XP,amd64", WINDOWS},
        {"Windows_95,x86", WINDOWS},
        {"Windows_95,amd64", WINDOWS},
        {"Windows_98,x86", WINDOWS},
        {"Windows_98,amd64", WINDOWS},
        {"Windows_Me,x86", WINDOWS},
        {"Windows_Me,amd64", WINDOWS},
        {"Windows_Vista,x86", WINDOWS},
        {"Windows_Vista,amd64", WINDOWS},
        {"Windows_7,x86", WINDOWS},
        {"Windows_7,amd64", WINDOWS},
        {"Windows_8,x86", WINDOWS},
        {"Windows_8,amd64", WINDOWS},
        {"Mac_OS_X,ppc", MAC}
    };

    /**
     * Table of data we'll measure
     */
    protected static final String[][] STARTUP_DATA = {
        {"ModuleSystem.readList finished", "ModuleSystem.readList finished, took ", "ms"},
        {"Preparation finished", "Preparation finished, took ", "ms"},
        {"Window system loaded", "Window system loaded dT=", ""},
        {"Window system shown", "Window system shown dT=", ""},
        {"Start", "IDE starts t = ", ""},
        {"End", "IDE is running t=", ""},
        {"Lookups set", "Lookups set dT=", ""}
    };
    public static final String separator = System.getProperty("file.separator");

    /**
     * Define testcase
     *
     * @param testName name of the testcase
     */
    public MeasureStartupTimeTestCase(String testName) {
        super(testName);
    }

    /**
     * Testing start of IDE with measurement of the startup time.
     *
     * @param performanceDataName name of the performance data
     * @throws IOException
     */
    protected void measureComplexStartupTime(String performanceDataName) throws IOException {
        // fail if prepare of the IDE failed
        if (new StatusFile().exists()) {
            fail("Prepare failed, so do the measurement of [" + performanceDataName + "]");
        }

        for (int i = 1; i <= repeat; i++) {
            long measuredTime = runIDEandMeasureStartup(performanceDataName, getMeasureFile(i), getUserdirFile(), 30000);
            reportPerformance(performanceDataName, measuredTime, "ms", 2);
        }
    }

    /**
     * Run IDE and read measured time from file
     *
     * @param performanceDataName name of the performance data
     * @param measureFile file where the time of window system painting is
     * stored
     * @param userdir userdir location
     * @param timeout wait after startup
     * @throws java.io.IOException
     * @return startup time
     */
    protected long runIDEandMeasureStartup(String performanceDataName, File measureFile, File userdir, long timeout) throws IOException {

        try {
            long startTime = runIDE(getIdeHome(), userdir, measureFile, timeout);
            HashMap<String, Long> measuredValues = parseMeasuredValues(measureFile);

            Object tempValue = measuredValues.get("IDE starts t = ");
            long runTime = 0;
            long endTime = 0;
            if (!(tempValue == null)) {
                runTime = ((Long) tempValue).longValue(); // from STARTUP_DATA
                measuredValues.remove("IDE starts t = "); // remove from measured values, the rest we will log as performance data
            }
            tempValue = measuredValues.get("IDE is running t=");
            if (!(tempValue == null)) {
                endTime = ((Long) tempValue).longValue(); // from STARTUP_DATA
                measuredValues.remove("IDE is running t="); // remove from measured values, the rest we will log as performance data
            }

            long startupTime = endTime - startTime;

            System.out.println("\t" + startTime + " -> run from command line (start) ");
            System.out.println("\t" + runTime + " -> time from -Dorg.netbeans.log.startup.logfile - IDE starts (run)");
            System.out.println("\t" + endTime + " -> time from RepaintManager - IDE is running (end)");
            System.out.println("Measured Startup Time=" + startupTime + " / IDE run=" + (runTime - startTime));

            if (startupTime <= 0) {
                fail("Measured value [" + startupTime + "] is not > 0 !");
            }
            reportPerformance(performanceDataName + " | IDE run", runTime - startTime, "ms", 1);

            for (String[] data : STARTUP_DATA) {
                if (measuredValues.containsKey(data[1])) {
                    long value = measuredValues.get(data[1]);
                    System.out.println(data[0] + "=" + value);
                    reportPerformance(performanceDataName + " | " + data[0], value, "ms", 1);
                } else {
                    System.out.println("Value for " + data[1] + " isn't present");
                }
            }

            return startupTime;
        } catch (CantParseMeasuredValuesException ex) {
            Logger.getLogger(MeasureStartupTimeTestCase.class.getName()).log(Level.SEVERE, null, ex);
            throw new AssertionError(ex);
        }
    }

    /**
     * Get platform on which the code is executed
     *
     * @return current platform name
     */
    protected static String getPlatform() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        if (osName.startsWith("windows")) {
            return WINDOWS;
        } else if (osName.startsWith("linux") || osName.startsWith("sun")) {
            return UNIX;
        } else if (osName.startsWith("mac")) {
            return MAC;
        } else {
            return UNKNOWN;
        }
    }

    /**
     * Creates and executes the command line for running IDE.
     *
     * @param ideHome IDE home directory
     * @param userdir User directory
     * @param measureFile file where measured time is stored
     * @param timeout wait after startup
     * @return measured time
     * @throws IOException
     */
    protected static long runIDE(File ideHome, File userdir, File measureFile, long timeout) throws IOException {
        return runIDE(ideHome, userdir, measureFile, timeout, null);
    }

    static long runIDE(File ideHome, File userdir, File measureFile, long timeout, File[] clusters) throws IOException {
        //add guitracker on classpath
        String classpath = ideHome.getAbsolutePath() + separator + "java" + separator + "modules" + separator + "org-netbeans-modules-performance.jar";

        //add property on command line
        String test_cmd_suffix = System.getProperty("xtest.perf.commandline.suffix");

        // create jdkhome switch
        String jdkhome = System.getProperty("java.home");
        if (jdkhome.endsWith("jre")) {
            jdkhome = jdkhome.substring(0, jdkhome.length() - 4);
        }

        File ideBinDir = new File(ideHome, "bin");

        String executor;

        if (getPlatform().equals(WINDOWS)) {
            executor = "netbeans.exe";
        } else {
            executor = "netbeans";
        }

        // construct command line
        StringBuffer cmd = new StringBuffer();
        String execDir = System.getProperty("netbeans.performance.exec.dir");
        if (execDir == null) {
            execDir = (new File(ideBinDir, executor)).getAbsolutePath();
        }
        cmd.append(execDir);

        // add other argumens
        // guiltracker lib
        cmd.append(" --cp:a ").append(classpath);
        // userdir
        // TODO: Check if this is really necessary to not quote paths without spaces
        String userdirPath = userdir.getAbsolutePath();
        if (userdirPath.indexOf(' ') >= 0) {
            cmd.append(" --userdir \"").append(userdirPath).append('\"');
        } else {
            cmd.append(" --userdir ").append(userdirPath);
        }
        // get jdkhome path
        // TODO: Check if this is really necessary to not quote paths without spaces
        if (jdkhome.indexOf(' ') >= 0) {
            cmd.append(" --jdkhome \"").append(jdkhome).append('\"');
        } else {
            cmd.append(" --jdkhome ").append(jdkhome);
        }
        // netbeans full hack
        cmd.append(" -J-Dnetbeans.full.hack=true");
        // measure argument
        // TODO: Check if this is really necessary to not quote paths without spaces
        String measureFilePath = measureFile.getAbsolutePath();
        if (measureFilePath.indexOf(' ') >= 0) {
            cmd.append(" -J-Dorg.netbeans.log.startup.logfile=\"").append(measureFilePath).append('\"');
        } else {
            cmd.append(" -J-Dorg.netbeans.log.startup.logfile=").append(measureFilePath);
        }
        // measure argument - we have to set this one to ommit repaint of memory toolbar (see openide/actions/GarbageCollectAction)
        cmd.append(" -J-Dorg.netbeans.log.startup=tests");
        // close the IDE after startup
        cmd.append(" -J-Dnetbeans.close=true");
        // use faster but more consistent random generator
        cmd.append(" -J-Dorg.netbeans.CLIHandler.fast.random=true");
        // wait after startup, need to set longer time for complex startup because rescan rises
//        cmd.append(" -J-Dorg.netbeans.performance.waitafterstartup=").append(timeout);
        cmd.append(" -J-Dnetbeans.logger.console=false");
        // disable status line displayer - issue 90542
        cmd.append(" -J-Dorg.openide.awt.StatusDisplayer.DISPLAY_TIME=0");
        // test command line suffix
        if (test_cmd_suffix != null && !test_cmd_suffix.equalsIgnoreCase("${xtest.perf.commandline.suffix}")) {
            cmd.append(' ').append(test_cmd_suffix.replace('\'', ' ').trim());
        }

        System.out.println("-----------------------------------------------");
        System.out.println("Running: " + cmd);

        Runtime runtime = Runtime.getRuntime();

        long startTime = System.nanoTime() / 1000000;

        // need to create out and err handlers
        Process ideProcess = runtime.exec(cmd.toString(), null, ideBinDir);

        // track out and errs from ide - the last parameter is PrintStream where the
        // streams are copied - currently set to null, so it does not hit performance much        
        // TODO: Remove System.out which is used to catch errors from the running IDE
        ThreadReader sout = new ThreadReader(ideProcess.getInputStream(), System.out);
        ThreadReader serr = new ThreadReader(ideProcess.getErrorStream(), System.out);
        try {
            System.out.println("IDE exited with status = " + ideProcess.waitFor());
        } catch (InterruptedException ie) {
            ie.printStackTrace(System.err);
            throw new IOException("Caught InterruptedException :" + ie.getMessage(), ie);
        }

        return startTime;
    }

    /**
     * Get IDE home directory.
     *
     * @throws IOException
     * @return IDE home directory
     */
    protected static File getIdeHome() throws IOException {
        String nbHome = System.getProperty("netbeans.dest.dir");
        File ideHome;
        if (nbHome != null) {
            ideHome = new File(nbHome);
        } else {
            ideHome = findPlatform().getParentFile();
        }
        if (!ideHome.isDirectory()) {
            throw new IOException("Cannot found netbeans.dest.dir - supplied value is " + nbHome);
        }
        return ideHome;
    }

    private static File findPlatform() {
        try {
            Class<?> lookup = Class.forName("org.openide.util.Lookup"); // NOI18N
            File util = new File(lookup.getProtectionDomain().getCodeSource().getLocation().toURI());
            assertTrue("Util exists: " + util, util.exists());

            return util.getParentFile().getParentFile();
        } catch (Exception ex) {
            try {
                File nbjunit = new File(MeasureStartupTimeTestCase.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                File harness = nbjunit.getParentFile().getParentFile();
                assertEquals("NbJUnit is in harness", "harness", harness.getName());
                TreeSet<File> sorted = new TreeSet<>();
                for (File p : harness.getParentFile().listFiles()) {
                    if (p.getName().startsWith("platform")) {
                        sorted.add(p);
                    }
                }
                assertFalse("Platform shall be found in " + harness.getParent(), sorted.isEmpty());
                return sorted.last();
            } catch (Exception ex2) {
                fail("Cannot find utilities JAR: " + ex + " and: " + ex2);
            }
            return null;
        }
    }

    /**
     * Get User directory
     *
     * @param n number of the userdir
     * @throws IOException
     * @return User directory
     */
    protected File getUserdirFile(int n) throws IOException {
        return new File(getWorkDir(), "ideuserdir_" + n);
    }

    /**
     * Get User directory. User directory is prepared and defined by property
     * <br> <code> userdir.prepared </code>.
     *
     * @throws IOException
     * @return User directory
     */
    protected File getUserdirFile() throws IOException {
        String usdPrep = System.getProperty("userdir.prepared");
        System.err.println("\n\n\nUSERDIR=" + usdPrep);
        if (usdPrep != null) {
            return new File(usdPrep);
        }
        return new File(getWorkDir(), "userdir0");
    }

    /**
     * Get file where measured time will be stored.
     *
     * @param i number of the new userdir used
     * @param j number of the run with the same userdir
     * @throws IOException
     * @return file where the measured time will be stored
     */
    protected File getMeasureFile(int i, int j) throws IOException {
        return new File(getWorkDir(), "measured_startup_" + i + "_" + j + ".txt");
    }

    /**
     * Get file where measured time will be stored.
     *
     * @param i number of the run with the same userdir
     * @throws IOException
     * @return file where the measured time will be stored
     */
    protected File getMeasureFile(int i) throws IOException {
        return new File(getWorkDir(), "measured_startup_" + i + ".txt");
    }

    protected static class CantParseMeasuredValuesException extends Exception {

        public CantParseMeasuredValuesException(Throwable cause) {
            super("Can't parse measured values", cause);
        }

    }

    /**
     * Parse logged startup time from the file.
     *
     * @param measuredFile file where the startup time is stored
     * @return measured startup time
     * @throws
     * org.netbeans.modules.performance.utilities.MeasureStartupTimeTestCase.CantParseMeasuredValuesException
     */
    protected static HashMap<String, Long> parseMeasuredValues(File measuredFile) throws CantParseMeasuredValuesException {
        HashMap<String, Long> measuredValues = new HashMap<>();

        HashMap<String, String> startup_data = new HashMap<>(STARTUP_DATA.length);
        for (String[] data : STARTUP_DATA) {
            startup_data.put(data[1], data[2]);
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(measuredFile));
            String readLine, value;
            int begin, end;
            while ((readLine = br.readLine()) != null && !startup_data.isEmpty()) {
                try {
                    for (String str : startup_data.keySet()) {
                        begin = readLine.indexOf(str);
                        if (begin != -1) {
                            end = readLine.indexOf(startup_data.get(str));

                            if (end <= begin) {
                                end = readLine.length();
                            }

                            value = readLine.substring(begin + str.length(), end);
                            measuredValues.put(str, new Long(value));
                            startup_data.remove(str);
                            break;
                        }

                    }

                } catch (NumberFormatException nfe) {
                    throw new CantParseMeasuredValuesException(nfe);
                }
            }
            return measuredValues;
        } catch (IOException ioe) {
            throw new CantParseMeasuredValuesException(ioe);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ioe) {
                    throw new CantParseMeasuredValuesException(ioe);
                }
            }
        }
    }

    /**
     * Create status file
     *
     * @throws java.io.IOException
     */
    public static void createStatusFile() throws IOException {
        new StatusFile().createNewFile();
    }

    public static class ThreadReader implements Runnable {

        private Thread thread;
        private final BufferedReader br;
        private final PrintStream out;
        private String s;

        public ThreadReader(InputStream in, PrintStream out) {
            br = new BufferedReader(new InputStreamReader(in));
            this.out = out;
            thread = new Thread(this);
            thread.start();
        }

        @Override
        public void run() {
            Thread myThread = Thread.currentThread();
            while (thread == myThread) {
                try {
                    s = br.readLine();
                    if (s == null) {
                        stop();
                        break;
                    } else {
                        if (out != null) {
                            out.println(s);
                        }
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace(System.err);
                    if (out != null) {
                        out.println("Caught IOE when reading IDE's out or err streams:" + ioe);
                    }
                    stop();
                }
            }
        }

        public void stop() {
            thread = null;
        }
    }

    private static class StatusFile extends File {

        /**
         * Creates a new instance of StatusFile
         *
         * @throws java.io.IOException
         */
        private StatusFile() throws IOException {
            super(CommonUtilities.getTempDir(), "prepare_failed.tmp");
        }

    }
}
