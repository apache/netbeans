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
package org.netbeans.performance.j2se.dialogs;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of Server Manager invoked from main menu.
 *
 * @author mmirilovic@netbeans.org
 */
public class ServerManagerTest extends PerformanceTestCase {

    /**
     * Creates a new instance of ServerManager
     *
     * @param testName test name
     */
    public ServerManagerTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of ServerManager
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public ServerManagerTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar")
                .addTest(ServerManagerTest.class)
                .suite();
    }

    public void testServerManager() {
        doMeasurement();
    }

    @Override
    public void prepare() {
    }

    @Override
    public ComponentOperator open() {
        String menu = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/Tools")
                + "|"
                + Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "CTL_ServerManager");
        MainWindowOperator.getDefault().menuBar().pushMenuNoBlock(menu);
        return new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.devmodules.api.Bundle", "TXT_ServerManager"));
    }
}
