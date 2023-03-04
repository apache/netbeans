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

import javax.swing.JButton;
import javax.swing.JComponent;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.PluginsOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of Proxy Configuration.
 *
 * @author mmirilovic@netbeans.org
 */
public class ProxyConfigurationTest extends PerformanceTestCase {

    private JButtonOperator openProxyButton;
    protected String BUTTON;
    private PluginsOperator pluginsOper;

    /**
     * Creates a new instance of ProxyConfiguration
     *
     * @param testName test name
     */
    public ProxyConfigurationTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of ProxyConfiguration
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public ProxyConfigurationTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar")
                .addTest(ProxyConfigurationTest.class)
                .suite();
    }

    public void testProxyConfiguration() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        repaintManager().addRegionFilter(new LoggingRepaintManager.RegionFilter() {

            @Override
            public boolean accept(JComponent c) {
                String className = c.getClass().getName();
                if ("javax.swing.JButton".equals(className) && "Proxy Settings".equals(((JButton)c).getText())) {
                    return false;
                }
                return !"javax.swing.JProgressBar".equals(c.getClass().getName());
            }

            @Override
            public String getFilterName() {
                return "Ignore search field cursor and Proxy Settings button animation";
            }
        });
        BUTTON = Bundle.getStringTrimmed("org.netbeans.modules.autoupdate.ui.Bundle", "SettingsTab.bProxy.text");
        pluginsOper = PluginsOperator.invoke();
        pluginsOper.selectSettings();
    }

    @Override
    public void prepare() {
        openProxyButton = new JButtonOperator(pluginsOper, BUTTON);
    }

    @Override
    public ComponentOperator open() {
        openProxyButton.pushNoBlock();
        return new NbDialogOperator("Options");
    }

    @Override
    public void close() {
        if (testedComponentOperator != null && testedComponentOperator.isShowing()) {
            ((NbDialogOperator) testedComponentOperator).close();
        }
    }
    
    @Override
    public void shutdown() {
        if (pluginsOper != null && pluginsOper.isShowing()) {
            pluginsOper.close();
        }
    }
}
