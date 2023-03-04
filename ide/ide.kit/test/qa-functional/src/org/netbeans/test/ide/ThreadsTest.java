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

package org.netbeans.test.ide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbModuleSuite;

/**
 * Threads test
 * see details on http://wiki.netbeans.org/FitnessViaWhiteAndBlackList
 *
 * @author mrkam@netbeans.org
 */
public class ThreadsTest extends JellyTestCase {

    private Set<String> allowedThreads;

    public ThreadsTest(String name) throws IOException {
        super(name);
        allowedThreads = new HashSet<String>();
        InputStream is = ThreadsTest.class.getResourceAsStream("allowed-threads.txt");
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        for (;;) {
            String line = r.readLine();
            if (line == null) {
                break;
            }
            if (line.startsWith("#")) {
                continue;
            }
            allowedThreads.add(line);
        }
        // System threads
        allowedThreads.add("Finalizer");
        allowedThreads.add("AWT-Windows");
    }
    
    public static Test suite() throws IOException {

        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(
            ThreadsTest.class
        ).clusters(".*").enableModules(".*").gui(true).reuseUserDir(false)
        .honorAutoloadEager(true);

        conf = conf.addTest("testThreads");
        
        return conf.suite();
    }

    public void testThreads() throws Exception {
        try {
            assertThreads();
        } catch (Error e) {
            e.printStackTrace(getLog("threads-report.txt"));
            throw e;
        }
    }

    public void assertThreads() {
        Map<Thread, StackTraceElement[]> data = Thread.getAllStackTraces();
        StringWriter msgs = new StringWriter();
        boolean fail = false;
        msgs.append("assertThreads:\n");
        for (Thread t : data.keySet()) {
            if (!acceptThread(t, data.get(t))) {
                msgs.append("assertThread: ").append(t.getName()).append('\n');
                for (StackTraceElement s : data.get(t)) {
                    msgs.append("    ").append(s.toString()).append('\n');
                }
                fail = true;
            }
        }
        assertFalse(msgs.toString(), fail);
    }

    public boolean acceptThread(Thread t, StackTraceElement[] stack) {
        if (allowedThreads.contains(t.getName())) {
            return true;
        }
        for (StackTraceElement elem : stack) {
            if (elem.toString().startsWith("org.openide.util.RequestProcessor$Processor.run")) {
                return true;
            }
            if (elem.toString().startsWith("java.util.TimerThread.run")) {
                return true;
            }
            if (elem.toString().startsWith("java.util.prefs.AbstractPreferences$EventDispatchThread")) {
                return true;
            }
            if (elem.toString().startsWith("sun.awt.image.ImageFetcher.run")) {
                return true;
            }
            if (elem.toString().startsWith("sun.awt.X11.XToolkit")) {
                return true;
            }
            if (elem.toString().startsWith("sun.java2d.d3d.D3DScreenUpdateManager")) {
                return true;
            }
        }
        return false;
    }

}

