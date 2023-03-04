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

import javax.swing.JComboBox;
import junit.framework.Test;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.SearchResultsOperator;
import org.netbeans.jellytools.actions.FindAction;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of Find Usages
 *
 * @author mmirilovic@netbeans.org
 */
public class SearchTest extends PerformanceTestCase {

    private NbDialogOperator findOper;

    /**
     * Creates a new instance of RefactorFindUsagesDialog
     *
     * @param testName test name
     */
    public SearchTest(String testName) {
        super(testName);
        expectedTime = 1000;
    }

    /**
     * Creates a new instance of RefactorFindUsagesDialog
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public SearchTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 1000;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(SearchTest.class)
                .suite();
    }

    public void testFindInProjects() {
        doMeasurement();
    }

    @Override
    public void initialize() {
    }

    @Override
    public void prepare() {
        new FindAction().perform(new ProjectsTabOperator().getProjectRootNode("PerformanceTestData"));
        findOper = new NbDialogOperator("Find in Projects");
        new JComboBoxOperator((JComboBox) new JLabelOperator(findOper, "Containing Text").getLabelFor()).typeText("public");
    }

    @Override
    public ComponentOperator open() {
        new JButtonOperator(findOper, "Find").push();
        SearchResultsOperator srchOp = new SearchResultsOperator();
        srchOp.waitEndOfSearch();
        return srchOp;
    }

    @Override
    public void close() {
        new SearchResultsOperator().close();
    }
}
