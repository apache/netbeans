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

/*
 * CPUType.java
 *
 * Created on July 19, 2005, 5:20 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package org.netbeans.lib.profiler.tests.jfluid.benchmarks;

import org.netbeans.lib.profiler.ProfilerEngineSettings;
import org.netbeans.lib.profiler.TargetAppRunner;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.tests.jfluid.*;
import org.netbeans.lib.profiler.tests.jfluid.utils.DumpStream;
import org.netbeans.lib.profiler.tests.jfluid.utils.TestProfilerAppHandler;
import org.netbeans.lib.profiler.tests.jfluid.utils.TestProfilingPointsProcessor;
import org.netbeans.lib.profiler.tests.jfluid.utils.Utils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;


/**
 *
 * @author ehucka
 */
public abstract class JbbTestType extends CommonProfilerTestCase {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance of CPUType
     */
    public JbbTestType(String name) {
        super(name);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    protected int[] checkResults(File workdir) {
        try {
            File rawres = new File(workdir, "results/SPECjbbSingleJVM/SPECjbb.001.raw");
            log(rawres.getAbsolutePath() + " exists " + rawres.exists());

            BufferedInputStream inp = new BufferedInputStream(new FileInputStream(rawres));
            Properties props = new Properties();
            props.load(inp);
            inp.close();

            int[] ret = new int[2];
            ret[0] = (int) Double.parseDouble(props.getProperty("result.test1.company.score"));
            //ret[0][1]=(int)Double.parseDouble(props.getProperty("result.test1.company.heapused"));
            ret[1] = (int) Double.parseDouble(props.getProperty("result.test2.company.score"));

            //ret[1][1]=(int)Double.parseDouble(props.getProperty("result.test2.company.heapused"));
            return ret;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    protected void evalueateResults(int[] without, int[] with, int maxSlowdown) {
        double[] diffs = new double[with.length];

        for (int i = 0; i < diffs.length; i++) {
            diffs[i] = (100.0 * (without[i] - with[i])) / with[i];
        }

        int maxmult = 0;
        log("\n\nTests     Normal    Profiler  Diff%     Multiple");

        for (int i = 0; i < diffs.length; i++) {
            log(complete("test " + String.valueOf(i + 1), 10) + complete(String.valueOf(without[i]), 10)
                + complete(String.valueOf(with[i]), 10) + complete(String.valueOf(diffs[i]), 6) + " %  "
                + complete(String.valueOf(without[i] / with[i]), 8));

            if (maxmult < (without[i] / with[i])) {
                maxmult = without[i] / with[i];
            }
        }

        log("\nMax multiple: " + String.valueOf(maxmult) + " Max allowed: "+maxSlowdown+"\n");
        assertTrue("Difference multiple is greater than "+maxSlowdown+" - " + String.valueOf(maxmult), (maxmult <= maxSlowdown));
    }

    protected ProfilerEngineSettings initCpuTest(String projectName, String mainClass) {
        ProfilerEngineSettings settings = initTest(projectName, mainClass, null);

        //defaults
        //addJVMArgs(settings, "-Dorg.netbeans.lib.profiler.wireprotocol.WireIO=true");
        String xData = new File(getDataDir(), "/projects/" + projectName + "/config.properties").getAbsolutePath();
        addJVMArgs(settings, "-Xms256m -Xmx256m");
        settings.setMainArgs("-propfile " + xData);
        settings.setThreadCPUTimerOn(false);
        settings.setCPUProfilingType(CommonConstants.CPU_INSTR_FULL);

        Utils.copyFolder(new File(getDataDir(), "/projects/" + projectName + "/xml"), new File(settings.getWorkingDir(), "xml"));

        return settings;
    }

    protected void startBenchmarkTest(ProfilerEngineSettings settings, int maxSlowdown) {
        //without profiler
        log("without profiler");
        log("*******************************************************************************");

        ArrayList command = new ArrayList(20);
        command.add(settings.getTargetJVMExeFile());
        command.add("-cp");
        command.add(settings.getMainClassPath());

        String[] args = settings.getJVMArgs();

        for (int i = 0; i < args.length; i++) {
            command.add(args[i]);
        }

        command.add(settings.getMainClassName());
        args = settings.getMainArgs();

        for (int i = 0; i < args.length; i++) {
            command.add(args[i]);
        }

        try {
            Process p = Runtime.getRuntime()
                               .exec((String[]) (command.toArray(new String[0])), null,
                                     new File(settings.getWorkingDir()));
            new DumpStream(p, p.getErrorStream(), getLogStream(), "[App error] ").start();
            new DumpStream(p, p.getInputStream(), getLogStream(), "[App output] ").start();
            p.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        File workdir = new File(settings.getWorkingDir());
        int[] res1 = checkResults(workdir);
        TargetAppRunner runner = new TargetAppRunner(settings, new TestProfilerAppHandler(this),
                                                     new TestProfilingPointsProcessor());

        try {
            Utils.removeFolder(new File(workdir, "results"));
            log("with profiler");
            log("*******************************************************************************");
            runner.addProfilingEventListener(Utils.createProfilingListener(this));

            runner.readSavedCalibrationData();
            runner.getProfilerClient().initiateRecursiveCPUProfInstrumentation(settings.getInstrumentationRootMethods());

            long time = System.currentTimeMillis();
            Process p = startTargetVM(runner);
            assertNotNull("Target JVM is not started", p);
            time = System.currentTimeMillis();
            runner.attachToTargetVMOnStartup();
            //Thread.sleep(delay);//wait for init
            waitForStatus(STATUS_RUNNING);
            assertTrue("runner is not running", runner.targetAppIsRunning());

            ArrayList metods = new ArrayList();
            long checkDelay = 1500;
            
            while (!isStatus(STATUS_APP_FINISHED) && !isStatus(STATUS_ERROR)) {
                time = System.currentTimeMillis();
                Thread.sleep(checkDelay);

                //do nothing
            }

            setStatus(STATUS_MEASURED);
            log("finish ****************************** " + getStatus());

            int[] res2 = checkResults(workdir);
            evalueateResults(res1, res2, maxSlowdown);
        } catch (Exception ex) {
            log(ex);
            assertTrue("Exception thrown: " + ex.getMessage(), false);
        } finally {
            finalizeTest(runner);
        }
    }
}
