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

package org.netbeans.lib.profiler.tests.jfluid.monitor;

import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.lib.profiler.ProfilerEngineSettings;


/**
 *
 * @author ehucka
 */
public class BasicTest extends MonitorTestCase {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of BasicTest */
    public BasicTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(BasicTest.class).addTest(
            "testBasic",
            "testBasicCPU",
            "testBasicMemory",
            "testCascadeThreads",
            "testCascadeThreadsCPU",
            "testCascadeThreadsMemory").enableModules(".*").clusters(".*").gui(false));
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void testBasic() {
        ProfilerEngineSettings settings = initMonitorTest("j2se-simple", "simple.Monitor");
        startMonitorTest(settings, 12, 1000, new String[] { "main", "Consumer", "Producer" },
                         new byte[][] {
                             { ST_SLEEPING | ST_WAIT | ST_RUNNING | ST_MONITOR },
                             { ST_UNKNOWN | ST_ZOMBIE, ST_WAIT | ST_RUNNING | ST_MONITOR },
                             { ST_UNKNOWN | ST_ZOMBIE, ST_WAIT | ST_RUNNING | ST_SLEEPING | ST_MONITOR }
                         }, MONITOR_ONLY);
    }

    public void testBasicCPU() {
        ProfilerEngineSettings settings = initMonitorTest("j2se-simple", "simple.Monitor");
        startMonitorTest(settings, 12, 1000, new String[] { "main", "Consumer", "Producer" },
                         new byte[][] {
                             { ST_SLEEPING | ST_WAIT | ST_RUNNING | ST_MONITOR },
                             { ST_UNKNOWN | ST_ZOMBIE, ST_WAIT | ST_RUNNING | ST_MONITOR },
                             { ST_UNKNOWN | ST_ZOMBIE, ST_WAIT | ST_RUNNING | ST_SLEEPING | ST_MONITOR }
                         }, WITH_CPU);
    }

    public void testBasicMemory() {
        ProfilerEngineSettings settings = initMonitorTest("j2se-simple", "simple.Monitor");
        startMonitorTest(settings, 12, 1000, new String[] { "main", "Consumer", "Producer" },
                         new byte[][] {
                             { ST_SLEEPING | ST_WAIT | ST_RUNNING | ST_MONITOR },
                             { ST_UNKNOWN | ST_ZOMBIE, ST_WAIT | ST_RUNNING | ST_MONITOR },
                             { ST_UNKNOWN | ST_ZOMBIE, ST_WAIT | ST_RUNNING | ST_SLEEPING | ST_MONITOR }
                         }, WITH_MEMORY);
    }

    public void testCascadeThreads() {
        ProfilerEngineSettings settings = initMonitorTest("j2se-simple", "simple.monitor.Monitor1");
        startMonitorTest(settings, 12, 1000, new String[] { "main", "Cascade" },
                         new byte[][] {
                             { ST_SLEEPING | ST_WAIT | ST_RUNNING | ST_MONITOR, ST_ZOMBIE },
                             { ST_UNKNOWN | ST_ZOMBIE, ST_RUNNING, ST_ZOMBIE }
                         }, MONITOR_ONLY);
    }

    public void testCascadeThreadsCPU() {
        ProfilerEngineSettings settings = initMonitorTest("j2se-simple", "simple.monitor.Monitor1");
        startMonitorTest(settings, 12, 1000, new String[] { "main", "Cascade" },
                         new byte[][] {
                             { ST_SLEEPING | ST_WAIT | ST_RUNNING | ST_MONITOR, ST_ZOMBIE },
                             { ST_UNKNOWN | ST_ZOMBIE, ST_RUNNING, ST_ZOMBIE }
                         }, WITH_CPU);
    }

    public void testCascadeThreadsMemory() {
        ProfilerEngineSettings settings = initMonitorTest("j2se-simple", "simple.monitor.Monitor1");
        startMonitorTest(settings, 12, 1000, new String[] { "main", "Cascade" },
                         new byte[][] {
                             { ST_UNKNOWN | ST_SLEEPING | ST_WAIT | ST_RUNNING | ST_MONITOR, ST_ZOMBIE },
                             { ST_UNKNOWN | ST_ZOMBIE, ST_RUNNING, ST_ZOMBIE }
                         }, WITH_MEMORY);
    }

    /*public void testGUICPU() {
       ProfilerEngineSettings settings = initMonitorTest("j2se-java2demo", "java2d.Intro");
       startMonitorTest(settings, 30, 1000, new String[] {"main"},
               new byte[][] {{ST_UNKNOWN|ST_ZOMBIE, ST_SLEEPING|ST_WAIT|ST_RUNNING|ST_MONITOR,ST_UNKNOWN|ST_ZOMBIE}}, WITH_CPU);
       }*/
}
