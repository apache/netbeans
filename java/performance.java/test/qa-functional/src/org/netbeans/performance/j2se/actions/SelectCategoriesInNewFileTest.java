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

import javax.swing.JComponent;
import junit.framework.Test;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of expanding nodes in the New File Wizard tree.
 *
 * @author mmirilovic@netbeans.org
 */
public class SelectCategoriesInNewFileTest extends PerformanceTestCase {

    /**
     * Category name
     */
    private static String category;
    /**
     * Jelly Operator for New Wizard
     */
    private static NewFileWizardOperator newFile;

    /**
     * Creates a new instance of SelectCategoriesInNewFile
     *
     * @param testName the name of the test
     */
    public SelectCategoriesInNewFileTest(String testName) {
        super(testName);
        expectedTime = 200;
        WAIT_AFTER_OPEN = 500;
    }

    /**
     * Creates a new instance of SelectCategoriesInNewFile
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public SelectCategoriesInNewFileTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 200;
        WAIT_AFTER_OPEN = 500;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(SelectCategoriesInNewFileTest.class)
                .suite();
    }

    public void testSelectGUIForms() {
        category = "Swing GUI Forms";
        doMeasurement();
    }

    public void testSelectXML() {
        category = "XML";
        doMeasurement();
    }

    public void testSelectOther() {
        category = "Other";
        doMeasurement();
    }

    @Override
    protected void initialize() {
        repaintManager().addRegionFilter(new LoggingRepaintManager.RegionFilter() {

            @Override
            public boolean accept(JComponent c) {
                return !c.getClass().getName().contains("ScrollBar");
            }

            @Override
            public String getFilterName() {
                return "Ignore scroll bar repaints";
            }
        });
    }

    @Override
    public void prepare() {
        newFile = NewFileWizardOperator.invoke();
    }

    @Override
    public ComponentOperator open() {
        newFile.selectCategory(category);
        return null;
    }

    @Override
    public void close() {
        newFile.cancel();
    }
    
    @Override
    public void shutdown() {
        repaintManager().resetRegionFilters();
    }
}
