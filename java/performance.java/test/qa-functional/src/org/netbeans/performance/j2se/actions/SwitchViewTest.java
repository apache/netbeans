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
package org.netbeans.performance.j2se.actions;

import javax.swing.KeyStroke;
import junit.framework.Test;
import org.netbeans.jellytools.FavoritesOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test view switching.
 *
 */
public class SwitchViewTest extends PerformanceTestCase {

    private static boolean initialized = false;
    private String operator;
    private int ke;

    /**
     * Creates a new instance of SwitchToFile
     *
     * @param testName test name
     */
    public SwitchViewTest(String testName) {
        super(testName);
        expectedTime = 150;
        WAIT_AFTER_OPEN = 200;
    }

    /**
     * Creates a new instance of SwitchView
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public SwitchViewTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 150;
        WAIT_AFTER_OPEN = 200;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class).reuseUserDir(true)
                .addTest(SwitchViewTest.class)
                .suite();
    }

    public void testSwitchToServices() {
        ke = java.awt.event.KeyEvent.VK_5;
        operator = "Services";
        doMeasurement();
    }

    public void testSwitchToProjects() {
        ke = java.awt.event.KeyEvent.VK_1;
        operator = "Projects";
        doMeasurement();
    }

    public void testSwitchToFiles() {
        ke = java.awt.event.KeyEvent.VK_2;
        operator = "Files";
        doMeasurement();
    }

    public void testSwitchToFavorites() {
        ke = java.awt.event.KeyEvent.VK_3;
        operator = "Favorites";
        doMeasurement();
    }

    @Override
    protected void initialize() {
        if (!initialized) {
            ProjectsTabOperator.invoke().collapseAll();
            RuntimeTabOperator.invoke().collapseAll();
            FilesTabOperator.invoke().collapseAll();
            FavoritesOperator.invoke().collapseAll();
            initialized = true;
        }
    }

    @Override
    public void prepare() {
        if (!operator.equals("Projects")) {
            new TopComponentOperator("Projects").makeComponentVisible();
        } else {
            new TopComponentOperator("Services").makeComponentVisible();
        }
    }

    @Override
    public ComponentOperator open() {
        new Action(null, null, KeyStroke.getKeyStroke(ke, java.awt.event.KeyEvent.CTRL_MASK)).performShortcut();
        return null;
    }

    @Override
    protected void shutdown() {
        repaintManager().resetRegionFilters();
    }
}
