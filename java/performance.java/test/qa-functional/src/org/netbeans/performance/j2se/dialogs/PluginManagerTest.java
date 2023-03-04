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
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;
import org.netbeans.jellytools.Bundle;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Plugin Manager
 *
 * @author anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class PluginManagerTest extends PerformanceTestCase {

    protected String BUNDLE, MENU, TITLE;

    /**
     * Creates a new instance of PluginManager
     *
     * @param testName test name
     */
    public PluginManagerTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    /**
     * Creates a new instance of PluginManager
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public PluginManagerTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar")
                .addTest(PluginManagerTest.class)
                .suite();
    }

    public void testPluginManager() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        BUNDLE = "org.netbeans.modules.autoupdate.ui.actions.Bundle";
        MENU = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/Tools") + "|" + Bundle.getStringTrimmed(BUNDLE, "PluginManagerAction_Name");
        TITLE = Bundle.getStringTrimmed(BUNDLE, "PluginManager_Panel_Name");
    }

    @Override
    public void prepare() {
    }

    @Override
    public ComponentOperator open() {
        new ActionNoBlock(MENU, null).perform();
        return new WizardOperator(TITLE);
    }
}
