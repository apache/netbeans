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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

            synchronized final void waitForAWT() throws InterruptedException {
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
