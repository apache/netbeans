/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.performance.j2se.startup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.performance.utilities.MeasureStartupTimeTestCase;

/**
 * Measure warm up time by org.netbeans.core.perftool.StartLog. Number of starts
 * with new userdir is defined by property
 * <br> <code> org.netbeans.performance.repeat.with.new.userdir </code>
 * <br> and number of starts with old userdir is defined by property
 * <br> <code> org.netbeans.performance.repeat </code>
 *
 * @author Marian.Mirilovic@sun.com
 */
public class MeasureWarmUp extends MeasureStartupTimeTestCase {

    protected static final String warmup = "Warmup task executed ";
    protected static final String warmup_started = "Warmup started";
    protected static final String warmup_finished = "Warmup finished, took ";
    public static final String suiteName = "J2SE Startup suite";

    /**
     * Define testcase
     *
     * @param testName name of the testcase
     */
    public MeasureWarmUp(java.lang.String testName) {
        super(testName);
    }

    /**
     * Testing start of IDE with measurement of the startup time.
     *
     * @throws IOException
     */
    public void testWarmUp() throws IOException {
        for (int i = 1; i <= repeat; i++) {
            runIDEandMeasureWarmUp(getMeasureFile(i), getUserdirFile(i), 5000);
        }
        PerformanceData[] pData = this.getPerformanceData();
        for (PerformanceData pData1 : pData) {
            org.netbeans.modules.performance.utilities.CommonUtilities.processUnitTestsResults(this.getClass().getName(), System.getProperty("suitename"), pData1);
        }
    }

    /**
     * Run IDE and read measured time from file
     *
     * @param measureFile file where the time of window system painting is
     * stored
     * @throws java.io.IOException
     * @return startup time
     */
    private void runIDEandMeasureWarmUp(File measureFile, File userdir, long timeout) throws IOException {
        runIDEWarmUp(getIdeHome(), userdir, measureFile, timeout);
        HashMap<String, Long> measuredValues = parseMeasuredValues(measureFile);

        if (measuredValues == null) {
            fail("It isn't possible to measure Warm Up.");
        }

        for (Map.Entry<String, Long> entry : measuredValues.entrySet()) {
            String name = entry.getKey();
            Long value = entry.getValue();
            System.out.println(name + "=" + value);
            reportPerformance(name, value, "ms", 1);
        }
    }

    /**
     * Creates and executes the command line for running IDE.
     *
     * @param ideHome IDE home directory
     * @param userdir User directory
     * @param measureFile file where measured time is stored
     * @throws IOException
     */
    private static void runIDEWarmUp(File ideHome, File userdir, File measureFile, long timeout) throws IOException {

        //check <userdir>/lock file
        if (new File(userdir, "lock").exists()) {
            fail("Original Userdir is locked!");
        }

        //add guitracker on classpath
        String classpath = ideHome.getAbsolutePath() + separator + "java" + separator + "modules" + separator + "org-netbeans-modules-performance.jar";

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
            cmd.append((new File(ideBinDir, executor)).getAbsolutePath());
        } else {
            cmd.append(execDir);
        }

        // add other argumens
        // guiltracker lib
        cmd.append(" --cp:a ").append(classpath);
        // userdir
        cmd.append(" --userdir ").append(userdir.getAbsolutePath());
        // get jdkhome path
        cmd.append(" --jdkhome ").append(jdkhome);
        // netbeans full hack
        cmd.append(" -J-Dnetbeans.full.hack=true");
        // measure argument
        cmd.append(" -J-Dorg.netbeans.log.startup.logfile=").append(measureFile.getAbsolutePath());
        // measure argument - we have to set this one to ommit repaint of memory toolbar (see openide/actions/GarbageCollectAction)
        cmd.append(" -J-Dorg.netbeans.log.startup=tests");
        // close the IDE after startup
        cmd.append(" -J-Dnetbeans.close=true");
        // close the IDE after warmup
        cmd.append(" -J-Dnetbeans.warm.close=true");
        cmd.append(" -J-Dnetbeans.logger.console=false");
//        cmd.append(" -agentlib:jdwp=transport=dt_socket,address=localhost:1234");  //debugging

        System.out.println("Running: " + cmd);
        System.out.println("Userdir: " + userdir.getAbsolutePath());

        Runtime runtime = Runtime.getRuntime();

        // need to create out and err handlers
        Process ideProcess = runtime.exec(cmd.toString(), null, ideBinDir);

        // track out and errs from ide - the last parameter is PrintStream where the
        // streams are copied - currently set to null, so it does not hit performance much
//        PrintStream outp = new PrintStream(new File(measureFile.getAbsolutePath().replace("txt", "out")));
//        PrintStream errp = new PrintStream(new File(measureFile.getAbsolutePath().replace("txt", "err")));
//        ThreadReader sout = new ThreadReader(ideProcess.getInputStream(), outp);
//        ThreadReader serr = new ThreadReader(ideProcess.getErrorStream(), errp);
        try {
            int exitStatus = ideProcess.waitFor();
            System.out.println("IDE exited with status = " + exitStatus);
        } catch (InterruptedException ie) {
            ie.printStackTrace(System.err);
            throw new IOException("Caught InterruptedException :" + ie.getMessage(), ie);
        }
    }

    /**
     * Parse logged startup time from the file.
     *
     * @param measuredFile file where the startup time is stored
     * @return measured startup time
     */
    protected static HashMap<String, Long> parseMeasuredValues(File measuredFile) {
        HashMap<String, Long> measuredValues = new HashMap<>();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(measuredFile));
            String readLine, name;
            long value, time = 0;
            int begin;

            //read log file until "Warmup started"
            while ((readLine = br.readLine()) != null && readLine.indexOf(warmup_started) == -1);

            //start to parse
            while ((readLine = br.readLine()) != null) {
                try {
                    if ((begin = readLine.indexOf(warmup)) != -1) { // @10741 - Warmup running org.netbeans.core.ui.DnDWarmUpTask dT=53
                        time = getTime(readLine);
                        name = readLine.substring(begin + warmup.length(), readLine.indexOf(" ", begin + warmup.length()));
                        measuredValues.put(name, time);

                    } else if ((readLine.indexOf(warmup_finished)) != -1) { // @12059 - Warmup finished, took 1459ms
                        name = "Warmup finished";
                        value = Long.parseLong(readLine.substring(readLine.indexOf("took ") + "took ".length(), readLine.indexOf("ms")));
                        measuredValues.put(name, value);
                    }
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace(System.err);
                    return null;
                }
            }
            return measuredValues;
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace(System.err);
                    return null;
                }
            }
        }
    }

    /**
     * Parse start time for the next warmup task
     *
     * @param line line
     * @return time
     */
    protected static long getTime(String line) {
        if (line.indexOf("@") != -1 && line.indexOf("-") != -1) {
            return Long.parseLong(line.substring(line.indexOf(" dT=") + 4));
        }
        return 0;
    }

    /*
     @25078 - Warmup started
     @25078 - Warmup running org.netbeans.modules.java.JavaWarmUpTask dT=0
     @25187 - Warmup running org.netbeans.core.ui.warmup.ContextMenuWarmUpTask dT=109
     @25281 - Warmup running org.netbeans.core.ui.warmup.DnDWarmUpTask dT=94
     @25281 - Warmup running org.netbeans.core.ui.warmup.MenuWarmUpTask dT=0
     @28406 - Warmup running org.netbeans.modules.editor.EditorWarmUpTask dT=3125
     @30141 - Warmup running org.netbeans.modules.java.editor.JavaEditorWarmUpTask dT=1735
     @31172 - Warmup finished, took 6094ms
     */
}
