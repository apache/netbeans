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

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.windows.WindowManager;

public class GeneralSanityTest extends NbTestCase {

    public GeneralSanityTest(String name) {
        super(name);
    }
    
    public static Test suite() throws IOException {
        CountingSecurityManager.initWrites();

        // disable 'slowness detection'
        System.setProperty("org.netbeans.core.TimeableEventQueue.quantum", "100000");
        NbTestSuite s = new NbTestSuite();
        s.addTest(new GeneralSanityTest("testInitBlacklistedClassesHandler"));
        s.addTest(
            NbModuleSuite.createConfiguration(
                GeneralSanityTest.class
            ).gui(true).clusters(".*").enableModules(".*").
            honorAutoloadEager(true).
            addTest(
                "testWaitForUIReady",
                "testNoWrites",
                "testBlacklistedClassesHandler"
            )
        .suite());
        return s;
    }

    public void testNoWrites() throws Exception {
        String msg = "No writes during startup.\n" +
            "Writing any files to disk during start is inefficient and usualy unnecessary.\n" +
            "Consider using declarative registration in your layer.xml file, or delaying\n" +
            "the initialization of the whole subsystem till it is really used.\n" +
            "In case it is necessary to perform the write, you can modify the\n" +
            "'allowed-file-write.txt' file in ide.kit module. More details at\n" +
            "http://wiki.netbeans.org/FitnessViaWhiteAndBlackList";

        CountingSecurityManager.assertCounts(msg, 0);
        // disable further collecting of
        CountingSecurityManager.initialize("non-existent", CountingSecurityManager.Mode.CHECK_READ, null);
    }

    public void testInitBlacklistedClassesHandler() {
        String configFN = new File(getDataDir(), "BlacklistedClassesHandlerConfig.xml").getPath();
        BlacklistedClassesHandler bcHandler = BlacklistedClassesHandlerSingleton.getInstance();

        System.out.println("BlacklistedClassesHandler will be initialized with " + configFN);
        if (bcHandler.initSingleton(configFN)) {
            bcHandler.register();
            System.out.println("BlacklistedClassesHandler handler added");
        } else {
            fail("Cannot initialize blacklisted class handler");
        }
    }

    public void testWaitForUIReady() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        class R implements Runnable {
            int countDown = 10;

            public synchronized void run() {
                notifyAll();
                countDown--;
            }

            final synchronized void waitForAWT() throws InterruptedException {
                while (countDown > 0) {
                    WindowManager.getDefault().invokeWhenUIReady(this);
                    wait();
                }
            }
        }
        R r = new R();
        r.waitForAWT();

    }

    public void testBlacklistedClassesHandler() throws Exception {
        BlacklistedClassesHandler bcHandler = BlacklistedClassesHandlerSingleton.getBlacklistedClassesHandler();
        assertNotNull("BlacklistedClassesHandler should be available", bcHandler);
        if (bcHandler.isGeneratingWhitelist()) {
            bcHandler.saveWhiteList(getLog("whitelist.txt"));
        }
        try {
            if (bcHandler.hasWhitelistStorage()) {
                bcHandler.saveWhiteList();
                bcHandler.saveWhiteList(getLog("whitelist.txt"));
                bcHandler.reportDifference(getLog("diff.txt"));
                assertTrue(bcHandler.reportViolations(getLog("violations.xml"))
                        + bcHandler.reportDifference(), bcHandler.noViolations());
            } else {
                assertTrue(bcHandler.reportViolations(getLog("violations.xml")), bcHandler.noViolations());
            }
        } finally {
            bcHandler.unregister();
        }
    }

}
